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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An aggregation of multiple bindings
 *
 * @since Groovy 1.6
 */
public class AggregateBinding implements BindingUpdatable {

    protected boolean bound;

    // use linked hash set so order is preserved
    protected Set<BindingUpdatable> bindings = new LinkedHashSet<BindingUpdatable>();

    public void addBinding(BindingUpdatable binding) {
        if (binding == null || bindings.contains(binding)) return;
        if (bound) binding.bind(); // bind is idempotent, so no state checking
        bindings.add(binding);
    }

    public void removeBinding(BindingUpdatable binding) {
        bindings.remove(binding);
    }

    @Override
    public void bind() {
        if (!bound) {
            bound = true;
            for (BindingUpdatable binding : bindings) {
                binding.bind();
            }
        }
    }

    @Override
    public void unbind() {
        if (bound) {
            for (BindingUpdatable binding : bindings) {
                binding.unbind();
            }
            bound = false;
        }
    }

    @Override
    public void rebind() {
        if (bound) {
            unbind();
            bind();
        }
    }

    @Override
    public void update() {
        for (BindingUpdatable binding : bindings) {
            binding.update();
        }
    }

    @Override
    public void reverseUpdate() {
        for (BindingUpdatable binding : bindings) {
            binding.reverseUpdate();
        }
    }
}
