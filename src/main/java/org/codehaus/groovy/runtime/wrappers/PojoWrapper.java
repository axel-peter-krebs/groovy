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
package org.codehaus.groovy.runtime.wrappers;

import groovy.lang.MetaClass;

public class PojoWrapper extends Wrapper {
    protected final Object wrapped;
    protected MetaClass delegate;

    public PojoWrapper(final Object wrapped, final Class constrainedType) {
        super(constrainedType);
        this.wrapped = wrapped;
    }

    @Override
    public Object unwrap() {
        return this.wrapped;
    }

    /* (non-Javadoc)
     * @see groovy.lang.GroovyObject#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(final String property) {
        return this.delegate.getProperty(this.wrapped, property);
    }

    /* (non-Javadoc)
     * @see groovy.lang.GroovyObject#invokeMethod(java.lang.String, java.lang.Object)
     */
    @Override
    public Object invokeMethod(final String methodName, final Object arguments) {
        return this.delegate.invokeMethod(this.wrapped, methodName, arguments);
    }

    /* (non-Javadoc)
     * @see groovy.lang.GroovyObject#setMetaClass(groovy.lang.MetaClass)
     */
    @Override
    public void setMetaClass(final MetaClass metaClass) {
        this.delegate = metaClass;
    }

    /* (non-Javadoc)
     * @see groovy.lang.GroovyObject#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(final String property, final Object newValue) {
        this.delegate.setProperty(this.wrapped, property, newValue);
    }

    @Override
    protected Object getWrapped() {
        return this.wrapped;
    }

    @Override
    protected MetaClass getDelegatedMetaClass() {
        return this.delegate;
    }
}
