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
package org.codehaus.groovy.reflection;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaMember;
import org.codehaus.groovy.runtime.FormatHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class CachedConstructor extends ParameterTypes implements MetaMember {

    private final CachedClass clazz;
    private final Constructor cachedConstructor;
    private boolean makeAccessibleDone = false;
    private boolean accessAllowed = false;

    public CachedConstructor(final CachedClass clazz, final Constructor c) {
        this.clazz = clazz;
        this.cachedConstructor = c;
    }

    public CachedConstructor(final Constructor c) {
        this(ReflectionCache.getCachedClass(c.getDeclaringClass()), c);
    }

    public static CachedConstructor find(Constructor constructor) {
        CachedConstructor[] constructors = ReflectionCache.getCachedClass(constructor.getDeclaringClass()).getConstructors();
        for (CachedConstructor cachedConstructor : constructors) {
            if (cachedConstructor.cachedConstructor.equals(constructor))
                return cachedConstructor;
        }
        throw new RuntimeException("Couldn't find method: " + constructor);
    }

    private static GroovyRuntimeException createException(String init, Constructor constructor, Object[] argumentArray, Throwable e, boolean setReason) {
        return new GroovyRuntimeException(
            init
                + constructor
                + " with arguments: "
                + FormatHelper.toString(argumentArray)
                + " reason: "
                + e,
            setReason ? e : null);
    }

    @Override
    protected Class[] getPT() {
        return cachedConstructor.getParameterTypes();
    }

    //--------------------------------------------------------------------------

    @Override
    public String getName() {
        return cachedConstructor.getName();
    }

    @Override
    public int getModifiers() {
        return cachedConstructor.getModifiers();
    }

    @Override
    public boolean isSynthetic() {
        return cachedConstructor.isSynthetic();
    }

    @Override
    public String toString() {
        return cachedConstructor.toString();
    }

    public Object doConstructorInvoke(Object[] argumentArray) {
        argumentArray = coerceArgumentsToClasses(argumentArray);
        return invoke(argumentArray);
    }

    public Object invoke(Object[] argumentArray) {
        Constructor constr = cachedConstructor;

        if (isConstructorOfAbstractClass()) {
            throw createException("failed to invoke constructor: ", constr, argumentArray, new InstantiationException(), true);
        }

        makeAccessibleIfNecessary();

        try {
            return constr.newInstance(argumentArray);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof RuntimeException ? (RuntimeException) e.getCause() : new InvokerInvocationException(e);
        } catch (IllegalArgumentException e) {
            throw createException("failed to invoke constructor: ", constr, argumentArray, e, false);
        } catch (IllegalAccessException e) {
            throw createException("could not access constructor: ", constr, argumentArray, e, false);
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw createException("failed to invoke constructor: ", constr, argumentArray, e, true);
        }
    }

    public CachedClass getCachedClass() {
        return clazz;
    }

    public Class getDeclaringClass() {
        return cachedConstructor.getDeclaringClass();
    }

    public Constructor getCachedConstructor() {
        makeAccessibleIfNecessary();
        if (!accessAllowed) {
            AccessPermissionChecker.checkAccessPermission(cachedConstructor);
            accessAllowed = true;
        }
        return cachedConstructor;
    }

    private void makeAccessibleIfNecessary() {
        if (!makeAccessibleDone) {
            ReflectionUtils.makeAccessibleInPrivilegedAction(cachedConstructor);
            makeAccessibleDone = true;
        }
    }

    private boolean isConstructorOfAbstractClass() {
        return Modifier.isAbstract(cachedConstructor.getDeclaringClass().getModifiers());
    }
}
