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
package groovy.lang;

import org.apache.groovy.plugin.GroovyRunner;
import org.apache.groovy.plugin.GroovyRunnerRegistry;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl;
import org.codehaus.groovy.util.ReferenceBundle;
import org.codehaus.groovy.util.ReleaseInfo;

import java.util.Map;

public final class GroovySystem {
    /**
     * Reference to the Runtime Registry to be used by the Groovy run-time system to find classes capable of running scripts
     *
     * @deprecated use {@link GroovyRunnerRegistry}
     */
    @Deprecated
    public static final Map<String, GroovyRunner> RUNNER_REGISTRY = GroovyRunnerRegistry.getInstance();
    /**
     * If true then the MetaClass will only use reflection for method dispatch, property access, etc.
     */
    @Deprecated
    private static final boolean USE_REFLECTION;

    /**
     * Reference to the MetaClass Registry to be used by the Groovy run-time system to map classes to MetaClasses
     */
    private static final MetaClassRegistry META_CLASS_REGISTRY;
    private static boolean keepJavaMetaClasses = false;

    //
    //  TODO: make this initialization able to set useReflection true
    //  TODO: have some way of specifying another MetaClass Registry implementation
    //
    static {
        USE_REFLECTION = true;
        META_CLASS_REGISTRY = new MetaClassRegistryImpl();
    }

    private GroovySystem() {
        // Do not allow this class to be instantiated
    }

    @Deprecated
    public static boolean isUseReflection() {
        return USE_REFLECTION;
    }

    public static MetaClassRegistry getMetaClassRegistry() {
        return META_CLASS_REGISTRY;
    }

    public static boolean isKeepJavaMetaClasses() {
        return keepJavaMetaClasses;
    }

    public static void setKeepJavaMetaClasses(boolean keepJavaMetaClasses) {
        GroovySystem.keepJavaMetaClasses = keepJavaMetaClasses;
    }

    /**
     * This method can be used to ensure that no threaded created
     * by a reference manager will be active. This is useful if the Groovy
     * runtime itself is loaded through a class loader which should be disposed
     * off. Without calling this method and if a threaded reference manager is
     * active the class loader cannot be unloaded!
     * <p>
     * Per default no threaded manager will be used.
     *
     * @since 1.6
     */
    public static void stopThreadedReferenceManager() {
        ReferenceBundle.getSoftBundle().getManager().stopThread();
        ReferenceBundle.getWeakBundle().getManager().stopThread();
    }

    /**
     * Returns the groovy version
     */
    public static String getVersion() {
        return ReleaseInfo.getVersion();
    }

    /**
     * Returns the major and minor part of the groovy version excluding the point/patch part of the version.
     * E.g. 3.0.0, 3.0.0-SNAPSHOT, 3.0.0-rc-1 all have 3.0 as the short version.
     *
     * @since 3.0.1
     */
    public static String getShortVersion() {
        String full = getVersion();
        int firstDot = full.indexOf('.');
        int secondDot = full.indexOf('.', firstDot + 1);
        if (secondDot < 0) {
            throw new GroovyBugError("Unexpected version found: " + full);
        }
        return full.substring(0, secondDot);
    }
}
