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
package gls

import groovy.test.GroovyTestCase
import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException

@AutoFinal
@CompileStatic
abstract class CompilableTestSupport extends GroovyTestCase {

    protected String shouldNotCompile(String script) {
        try (def gcl = new GroovyClassLoader()) {
            gcl.parseClass(script, testClassName)
        } catch (CompilationFailedException ex) {
            return ex.message
        }
        fail('the compilation succeeded but should have failed')
    }

    protected void shouldCompile(final String script) {
        try (def gcl = new GroovyClassLoader()) {
            gcl.parseClass(script, testClassName)
        }
    }
}
