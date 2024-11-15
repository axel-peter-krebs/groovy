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
package org.apache.groovy.swing.binding;

import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PropertyChangeProxyTargetBinding implements TargetBinding {
    Object proxyObject;
    String propertyName;
    PropertyChangeListener listener;

    public PropertyChangeProxyTargetBinding(Object proxyObject, String propertyName, PropertyChangeListener listener) {
        this.proxyObject = proxyObject;
        this.propertyName = propertyName;
        this.listener = listener;
    }

    @Override
    public void updateTargetValue(Object value) {
        listener.propertyChange(new PropertyChangeEvent(proxyObject, propertyName,
            InvokerHelper.getProperty(proxyObject, propertyName), value));
    }
}
