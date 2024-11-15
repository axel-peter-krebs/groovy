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
package org.codehaus.groovy.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This ClassLoader should be used as root of class loaders. Any
 * RootLoader does have its own classpath. When searching for a
 * class or resource this classpath will be used. Parent
 * Classloaders are ignored first. If a class or resource
 * can't be found in the classpath of the RootLoader, then parent is
 * checked.
 * <p>
 * <b>Note:</b> this is very against the normal behavior of
 * classloaders. Normal is to first check parent and then look in
 * the resources you gave this classloader.
 * <p>
 * It's possible to add urls to the classpath at runtime through {@link #addURL(URL)}.
 * <p>
 * <b>Why using RootLoader?</b>
 * If you have to load classes with multiple classloaders and a
 * classloader does know a class which depends on a class only
 * a child of this loader does know, then you won't be able to
 * load the class. To load the class the child is not allowed
 * to redirect its search for the class to the parent first.
 * That way the child can load the class. If the child does not
 * have all classes to do this, this fails of course.
 * <p>
 * For example:
 * <p>
 * <pre>
 *       parentLoader   (has classpath: a.jar;c.jar)
 *           |
 *           |
 *       childLoader    (has classpath: a.jar;b.jar;c.jar)
 * </pre>
 * <p>
 * class C (from c.jar) extends B (from b.jar)
 * <p>
 * childLoader.find("C")
 * <pre>
 * --&gt; parentLoader does know C.class, try to load it
 * --&gt; to load C.class it has to load B.class
 * --&gt; parentLoader is unable to find B.class in a.jar or c.jar
 * --&gt; NoClassDefFoundException!
 * </pre>
 * <p>
 * if childLoader had tried to load the class by itself, there
 * would be no problem. Changing childLoader to be a RootLoader
 * instance will solve that problem.
 */
public class RootLoader extends URLClassLoader {

    private static final String ORG_W3C_DOM_NODE = "org.w3c.dom.Node";
    private final Map<String, Class<?>> customClasses = new HashMap<>();

    /**
     * Constructs a {@code RootLoader} without classpath.
     *
     * @param parent the parent Loader
     */
    public RootLoader(final ClassLoader parent) {
        this(new URL[0], parent);
    }

    /**
     * Constructs a {@code RootLoader} with a parent loader and an array of URLs
     * as its classpath.
     */
    public RootLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
        // major hack here!!
        try {
            customClasses.put(ORG_W3C_DOM_NODE, super.loadClass(ORG_W3C_DOM_NODE, false));
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Constructs a {@code RootLoader} with a {@link LoaderConfiguration} object
     * which holds the classpath.
     */
    public RootLoader(final LoaderConfiguration lc) {
        this(Optional.ofNullable(RootLoader.class.getClassLoader()).orElseGet(ClassLoader::getSystemClassLoader));

        Thread.currentThread().setContextClassLoader(this);

        for (URL url : lc.getClassPathUrls()) {
            addURL(url);
        }
        // TODO M12N eventually defer this until later when we have a full Groovy
        // environment and use normal Grape.grab()
        String groovyHome = System.getProperty("groovy.home");
        for (String url : lc.getGrabUrls()) {
            Map<String, Object> grabParts = GrapeUtil.getIvyParts(url);
            String group = (String) grabParts.get("group");
            String module = (String) grabParts.get("module");
            String version = (String) grabParts.get("version");
            File jar = new File(groovyHome + "/repo/" + group + "/" + module + "/jars/" + module + "-" + version + ".jar");
            try {
                addURL(jar.toURI().toURL());
            } catch (MalformedURLException e) {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null) return c;
        c = customClasses.get(name);
        if (c != null) return c;

        try {
            c = super.findClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        if (c == null)
            c = super.loadClass(name, resolve);

        if (resolve)
            resolveClass(c);

        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getResource(final String name) {
        URL url = findResource(name);
        if (url == null)
            url = super.getResource(name);
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addURL(final URL url) {
        super.addURL(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }
}
