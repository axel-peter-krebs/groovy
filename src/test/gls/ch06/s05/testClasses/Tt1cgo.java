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
package gls.ch06.s05.testClasses;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

public class Tt1cgo extends GroovyObjectSupport {
    public Closure x = new Closure(null) {
        public Object doCall(final Object params) {
            return "field";
        }
    };
    private Closure p1 = new Closure(null) {
        public Object doCall(final Object params) {
            return "property";
        }
    };

    public Closure getX() {
        return this.p1;
    }

    public void setX(final Closure x) {
        this.p1 = x;
    }

    public String x() {
        return "method";
    }
}
