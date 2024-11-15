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
package org.codehaus.groovy.control.customizers.builder;

import groovy.util.AbstractFactory;
import groovy.util.FactoryBuilderSupport;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * This factory generates an array of compilation customizers.
 *
 * @since 2.1.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomizersFactory extends AbstractFactory implements PostCompletionFactory {

    @Override
    public Object newInstance(final FactoryBuilderSupport builder, final Object name, final Object value, final Map attributes) throws InstantiationException, IllegalAccessException {
        return new ArrayList();
    }

    @Override
    public void setChild(final FactoryBuilderSupport builder, final Object parent, final Object child) {
        if (parent instanceof Collection && child instanceof CompilationCustomizer) {
            ((Collection) parent).add(child);
        }
    }

    @Override
    public Object postCompleteNode(final FactoryBuilderSupport factory, final Object parent, final Object node) {
        if (node instanceof Collection) {
            return ((Collection) node).toArray(new CompilationCustomizer[0]);
        }
        return node;
    }
}
