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
package org.apache.groovy.plugin;

import groovy.lang.GroovyClassLoader;

/**
 * Classes which can run scripts should implement this interface.
 *
 * @since 2.5.0
 */
public interface GroovyRunner {

    /**
     * Returns {@code true} if this runner is able to
     * run the given class.
     *
     * @param scriptClass class to run
     * @param loader      used to locate classes and resources
     * @return true if given class can be run, else false
     */
    boolean canRun(Class<?> scriptClass, GroovyClassLoader loader);

    /**
     * Runs the given class.
     *
     * @param scriptClass class to run
     * @param loader      used to locate classes and resources
     * @return result of running the class
     */
    Object run(Class<?> scriptClass, GroovyClassLoader loader);

}
