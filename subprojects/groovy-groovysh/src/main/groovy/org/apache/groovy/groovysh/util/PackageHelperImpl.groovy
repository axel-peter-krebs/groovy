/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.groovysh.util

import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.Preferences

import java.nio.file.InvalidPathException
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import java.util.regex.Pattern

/**
 * Helper class that crawls all items of the classpath for packages.
 * Retrieves from those sources the list of subpackages and classes on demand.
 */
@AutoFinal
@CompileStatic
class PackageHelperImpl implements PreferenceChangeListener, PackageHelper {

    protected static final Logger LOG = Logger.create(PackageHelperImpl)

    /** Pattern for regular class names. */
    public static final Pattern NAME_PATTERN = ~('^[A-Z][^.\$_]+\$')

    private static final String CLASS_SUFFIX = '.class'

    Map<String, CachedPackage> rootPackages
    final ClassLoader groovyClassLoader

    PackageHelperImpl(ClassLoader groovyClassLoader = null) {
        this.groovyClassLoader = groovyClassLoader
        initializePackages()
        Preferences.addChangeListener(this)
    }

    @Override
    void reset() {
        initializePackages()
    }

    private void initializePackages() {
        if (!Boolean.valueOf(Preferences.get(IMPORT_COMPLETION_PREFERENCE_KEY))) {
            rootPackages = getPackages(this.groovyClassLoader)
        }
    }

    @Override
    void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.key == IMPORT_COMPLETION_PREFERENCE_KEY) {
            if (Boolean.valueOf(evt.getNewValue())) {
                rootPackages = null
            } else if (rootPackages == null) {
                initializePackages()
            }
        }
    }

    private static Map<String, CachedPackage> getPackages(ClassLoader groovyClassLoader) throws IOException {
        Map<String, CachedPackage> rootPackages = new HashMap()
        Set<URL> urls = new HashSet<>()

        // classes in CLASSPATH
        for (ClassLoader loader = groovyClassLoader; loader != null; loader = loader.parent) {
            if (!(loader instanceof URLClassLoader)) {
                LOG.debug('Ignoring classloader for completion: ' + loader)
                continue
            }

            urls.addAll(((URLClassLoader) loader).URLs)
        }

        // System classes
        Class<?>[] systemClasses = [String, javax.swing.JFrame, GroovyObject] as Class[]
        boolean jigsaw = false
        systemClasses.each { Class systemClass ->
            // normal slash even in Windows
            String classfileName = systemClass.name.replace('.', '/') + CLASS_SUFFIX
            URL classURL = systemClass.getResource(classfileName)
            if (classURL == null) {
                // this seems to work on Windows better than the earlier approach
                classURL = Thread.currentThread().contextClassLoader.getResource(classfileName)
            }
            if (classURL != null) {
                URLConnection uc = classURL.openConnection()
                if (uc instanceof JarURLConnection) {
                    urls.add(((JarURLConnection) uc).getJarFileURL())
                } else if (uc.getClass().getSimpleName().equals("JavaRuntimeURLConnection")) {
                    // Java 9 Jigsaw detected
                    jigsaw = true
                } else {
                    String filepath = classURL.toExternalForm()
                    String rootFolder = filepath.substring(0, filepath.length() - classfileName.length() - 1)
                    urls.add(new URL(rootFolder))
                }
            }
        }

        for (URL url : urls) {
            Collection<String> packageNames = getPackageNames(url)
            if (packageNames) {
                mergeNewPackages(packageNames, url, rootPackages)
            }
        }
        if (jigsaw || isModularRuntime()) {
            URL jigsawURL = URI.create("jrt:/").toURL()
            Set<String> jigsawPackages = getPackagesAndClassesFromJigsaw(jigsawURL) { isPackage, name -> isPackage && name }
            mergeNewPackages(jigsawPackages, jigsawURL, rootPackages)
        }
        return rootPackages
    }

    // TODO: review after jdk9 is released
    @CompileDynamic
    private static boolean isModularRuntime() {
        try {
            return this.classLoader.loadClass('java.lang.reflect.Module', false) != null
        } catch (e) {
            return false
        }
    }

    /**
     * Returns packages or classes listed from Jigsaw modules. It makes use of a
     * GroovyShell in order to avoid a hard dependency to JDK 7+ when building
     * the Groovysh module (uses nio2).
     */
    private static Set<String> getPackagesAndClassesFromJigsaw(URL jigsawURL, Closure<Boolean> predicate) {
        def shell = new GroovyShell()
        shell.setProperty('predicate', predicate)
        String jigsawURLString = jigsawURL.toString()
        shell.setProperty('jigsawURLString', jigsawURLString)
        shell.evaluate '''import java.nio.file.*

def fs = FileSystems.newFileSystem(URI.create(jigsawURLString), [:])

result = [] as Set

def filterPackageName(Path path) {
    def elems = "$path".split('/')

    if (elems && elems.length > 2) {
        // remove e.g. 'modules/java.base/
        elems = elems[2..<elems.length]

        def name = elems.join('.')
        if (predicate(true, name)) {
            result << name
        }
    }
}

def filterClassName(Path path) {
    def elems = "$path".split('/')

    if (elems && elems.length > 2) {
        // remove e.g. 'modules/java.base/
        elems = elems[2..<elems.length]

        def name = elems.join('.')
        if (name.endsWith('.class')) {
            name = name.substring(0, name.lastIndexOf('.'))
            if (predicate(false, name)) {
                result << name
            }
        }
    }
}

class GroovyFileVisitor extends SimpleFileVisitor {}

// walk each file and directory, possibly storing directories as packages, and files as classes
Files.walkFileTree(fs.getPath('modules'),
        [preVisitDirectory: { dir, attrs -> filterPackageName(dir); FileVisitResult.CONTINUE },
         visitFile: { file, attrs -> filterClassName(file); FileVisitResult.CONTINUE}
        ]
            as GroovyFileVisitor)
'''

        Set<String> jigsawPackages = (Set<String>) shell.getProperty('result')

        jigsawPackages
    }

    static mergeNewPackages(Collection<String> packageNames, URL url, Map<String, CachedPackage> rootPackages) {
        StringTokenizer tokenizer
        packageNames.each { String packname ->
            tokenizer = new StringTokenizer(packname, '.')
            if (!tokenizer.hasMoreTokens()) {
                return
            }
            String rootname = tokenizer.nextToken()
            CachedPackage cp
            CachedPackage childp
            cp = rootPackages.get(rootname, null) as CachedPackage
            if (cp == null) {
                cp = new CachedPackage(rootname, [url] as Set)
                rootPackages.put(rootname, cp)
            }

            while (tokenizer.hasMoreTokens()) {
                String packbasename = tokenizer.nextToken()
                if (cp.childPackages == null) {
                    // small initial size, to save memory
                    cp.childPackages = new HashMap<String, CachedPackage>(1)
                }
                childp = cp.childPackages.get(packbasename, null) as CachedPackage
                if (childp == null) {
                    // start with small arraylist, to save memory
                    Set<URL> urllist = new HashSet<URL>(1)
                    urllist.add(url)
                    childp = new CachedPackage(packbasename, urllist)
                    cp.childPackages.put(packbasename, childp)
                } else {
                    childp.sources.add(url)
                }
                cp = childp
            }
        }
    }

    /**
     * Returns all package names found at URL; accepts jar files and folders.
     */
    static Collection<String> getPackageNames(URL url) {
        File urlFile = Paths.get(url.toURI()).toFile()

        if (urlFile.isDirectory()) {
            return new HashSet<String>().tap {
                collectPackageNamesFromFolderRecursive(urlFile, '', it)
            }
        }

        if (urlFile.path.endsWith('.jar')) {
            try {
                JarFile jarFile = new JarFile(urlFile)
                return getPackageNamesFromJar(jarFile)
            } catch (IOException | InvalidPathException e) {
                if (LOG.isDebugEnabled()) e.printStackTrace()
                LOG.warn("Error opening jar file : '${url.file}' : ${e.toString()}")
            }
        }

        return []
    }

    /**
     * Crawls a folder, iterates over subfolders, looking for class files.
     */
    static Collection<String> collectPackageNamesFromFolderRecursive(File directory, String prefix, Set<String> packnames) {
        File[] files = directory.listFiles()
        boolean packageAdded = false

        for (int i = 0; (files != null) && (i < files.length); i++) {
            if (files[i].isDirectory()) {
                if (files[i].name.startsWith('.')) {
                    return
                }
                String optionalDot = prefix ? '.' : ''
                collectPackageNamesFromFolderRecursive(files[i], prefix + optionalDot + files[i].name, packnames)
            } else if (!packageAdded) {
                if (files[i].name.endsWith(CLASS_SUFFIX)) {
                    packageAdded = true
                    if (prefix) {
                        packnames.add(prefix)
                    }
                }
            }
        }
    }

    static Collection<String> getPackageNamesFromJar(JarFile jf) {
        Set<String> packnames = new HashSet<>()
        for (Enumeration e = jf.entries(); e.hasMoreElements();) {
            JarEntry entry = (JarEntry) e.nextElement()

            if (entry == null) {
                continue
            }

            String name = entry.name

            if (!name.endsWith(CLASS_SUFFIX)) {
                // only use class files
                continue
            }
            // normal slashes also on Windows
            String fullname = name.replace('/', '.').substring(0, name.length() - CLASS_SUFFIX.length())
            // Discard classes in the default package
            if (fullname.lastIndexOf('.') > -1) {
                packnames.add(fullname.substring(0, fullname.lastIndexOf('.')))
            }
        }
        return packnames
    }

    /**
     * Returns the names of Classes and direct subpackages contained in a package.
     */
    @Override
    Set<String> getContents(String packagename) {
        if (!rootPackages) {
            return [] as Set
        }
        if (!packagename) {
            return rootPackages.collect { String key, CachedPackage v -> key } as Set
        }
        String sanitizedPackageName
        if (packagename.endsWith('.*')) {
            sanitizedPackageName = packagename[0..-3]
        } else {
            sanitizedPackageName = packagename
        }

        StringTokenizer tokenizer = new StringTokenizer(sanitizedPackageName, '.')
        CachedPackage cp = rootPackages.get(tokenizer.nextToken())
        while (cp != null && tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken()
            if (cp.childPackages == null) {
                // no match for taken,no subpackages known
                return [] as Set
            }
            cp = cp.childPackages.get(token) as CachedPackage
        }
        if (cp == null) {
            return [] as Set
        }
        // TreeSet for ordering
        Set<String> children = new TreeSet()
        if (cp.childPackages) {
            children.addAll(cp.childPackages.collect { String key, CachedPackage v -> key })
        }
        if (cp.checked && !cp.containsClasses) {
            return children
        }

        Set<String> classnames = getClassnames(cp.sources, sanitizedPackageName)

        cp.checked = true
        if (classnames) {
            cp.containsClasses = true
            children.addAll(classnames)
        }
        return children
    }

    /**
     * Copied from JLine 1.0 ClassNameCompletor.
     */
    static Set<String> getClassnames(Set<URL> urls, String packagename) {
        Set<String> classes = new TreeSet<>()
        // normal slash even in Windows
        String pathname = packagename.replace('.', '/')
        for (Iterator<URL> it = urls.iterator(); it.hasNext();) {
            URL url = it.next()
            if (url.protocol == 'jrt') {
                getPackagesAndClassesFromJigsaw(url) { boolean isPackage, String name ->
                    !isPackage && name.startsWith(packagename)
                }.collect(classes) { it - "${packagename}." }
            } else {
                File file = new File(URLDecoder.decode(url.getFile(), 'UTF-8'))
                if (file == null) {
                    continue
                }
                if (file.isDirectory()) {
                    File packFolder = new File(file, pathname)
                    if (!packFolder.isDirectory()) {
                        continue
                    }
                    File[] files = packFolder.listFiles()
                    for (int i = 0; (files != null) && (i < files.length); i++) {
                        if (files[i].isFile()) {
                            String filename = files[i].name
                            if (filename.endsWith(CLASS_SUFFIX)) {
                                String name = filename.substring(0, filename.length() - CLASS_SUFFIX.length())
                                if (!name.matches(NAME_PATTERN)) {
                                    continue
                                }
                                classes.add(name)
                            }
                        }
                    }
                    continue
                }

                if (!file.toString().endsWith('.jar')) {
                    continue
                }

                JarFile jf = new JarFile(file)

                try {
                    for (Enumeration e = jf.entries(); e.hasMoreElements();) {
                        JarEntry entry = (JarEntry) e.nextElement()

                        if (entry == null) {
                            continue
                        }

                        String name = entry.name

                        // only use class files
                        if (!name.endsWith(CLASS_SUFFIX)) {
                            continue
                        }
                        // normal slash inside jars even on windows
                        int lastslash = name.lastIndexOf('/')
                        if (lastslash == -1 || name.substring(0, lastslash) != pathname) {
                            continue
                        }
                        name = name.substring(lastslash + 1, name.length() - CLASS_SUFFIX.length())
                        if (!name.matches(NAME_PATTERN)) {
                            continue
                        }
                        classes.add(name)
                    }
                } finally {
                    jf.close()
                }
            }
        }


        return classes
    }
}

@CompileStatic
class CachedPackage {
    String name
    boolean containsClasses
    boolean checked
    Map<String, CachedPackage> childPackages
    Set<URL> sources

    CachedPackage(String name, Set<URL> sources) {
        this.name = name
        this.sources = sources
    }
}
