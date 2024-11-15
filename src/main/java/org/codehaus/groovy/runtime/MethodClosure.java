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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.util.Arrays;

/**
 * Represents a method on an object using a closure, which can be invoked at any
 * time.
 */
public class MethodClosure extends Closure {

    public static final String ANY_INSTANCE_METHOD_EXISTS = "anyInstanceMethodExists";
    public static final String NEW = "new";
    private static final long serialVersionUID = -2491254866810955844L;

    //
    public static boolean ALLOW_RESOLVE; // choose readObject/readResolve return/throw
    private final String method;
    /**
     * Indicates if this may be related to an instance method.
     */
    private boolean anyInstanceMethodExists;

    //--------------------------------------------------------------------------

    public MethodClosure(final Object owner, final String method) {
        super(owner);
        this.method = method;
        this.maximumNumberOfParameters = 0;
        this.parameterTypes = MetaClassHelper.EMPTY_TYPE_ARRAY;

        var ownerClass = getOwnerClass();

        if (method.equals(NEW)) {
            if (ownerClass.isArray()) {
                Class<?>[] sizeTypes = new Class[ArrayTypeUtils.dimension(ownerClass)];
                Arrays.fill(sizeTypes, int.class);
                setParameterTypesAndNumber(sizeTypes);
            } else {
                for (var c : ReflectionCache.getCachedClass(ownerClass).getConstructors()) {
                    setParameterTypesAndNumber(c.getNativeParameterTypes());
                }
            }
        } else {
            for (var m : InvokerHelper.getMetaClass(ownerClass).respondsTo(getOwner(), method)) {
                setParameterTypesAndNumber(makeParameterTypes(getOwner(), m));
                if (!m.isStatic()) {
                    this.anyInstanceMethodExists = true;
                }
            }
        }
    }

    /*
     * Creates an array of parameter types. If the owner is a class instance (ex:
     * String) and the method is instance method, we expand the original array of
     * parameter type by inserting the owner at the first position of the array.
     */
    private static Class[] makeParameterTypes(final Object owner, final MetaMethod m) {
        Class[] newParameterTypes;

        if (owner instanceof Class && !m.isStatic()) {
            Class[] nativeParameterTypes = m.getNativeParameterTypes();
            newParameterTypes = new Class[nativeParameterTypes.length + 1];

            System.arraycopy(nativeParameterTypes, 0, newParameterTypes, 1, nativeParameterTypes.length);
            newParameterTypes[0] = (Class) owner;
        } else {
            newParameterTypes = m.getNativeParameterTypes();
        }

        return newParameterTypes;
    }

    private void setParameterTypesAndNumber(final Class[] parameterTypes) {
        if (parameterTypes.length > this.maximumNumberOfParameters) {
            this.maximumNumberOfParameters = parameterTypes.length;
            this.parameterTypes = parameterTypes;
        }
    }

    //--------------------------------------------------------------------------

    public String getMethod() {
        return method;
    }

    @Override
    public Object getOwner() {
        var owner = super.getOwner();
        if (owner instanceof Wrapper) {
            owner = ((Wrapper) owner).unwrap(); // GROOVY-5051
        }
        return owner;
    }

    /**
     * @since 5.0.0
     */
    public Class<?> getOwnerClass() {
        var owner = super.getOwner();
        if (owner instanceof Wrapper) {
            return ((Wrapper) owner).getType(); // GROOVY-5051
        }
        var theClass = owner.getClass();
        return (theClass == Class.class ? (Class<?>) owner : theClass);
    }

    @Override
    public Object getProperty(final String property) {
        switch (property) {
            case "method":
                return getMethod();
            case ANY_INSTANCE_METHOD_EXISTS:
                return anyInstanceMethodExists;
            default:
                return super.getProperty(property);
        }
    }

    // TODO: This method seems to be never called..., because MetaClassImpl.invokeMethod will intercept calls and return the result.
    protected Object doCall(final Object arguments) {
        return InvokerHelper.invokeMethod(getOwner(), getMethod(), arguments);
    }

    private void readObject(final java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        if (ALLOW_RESOLVE) {
            stream.defaultReadObject();
        }
        throw new UnsupportedOperationException();
    }

    private Object readResolve() {
        if (ALLOW_RESOLVE) {
            return this;
        }
        throw new UnsupportedOperationException();
    }
}
