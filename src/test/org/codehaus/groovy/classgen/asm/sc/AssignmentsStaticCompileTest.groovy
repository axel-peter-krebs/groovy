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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.STCAssignmentTest

/**
 * Unit tests for static compilation : assignments.
 */
final class AssignmentsStaticCompileTest extends STCAssignmentTest implements StaticCompilationTestSupport {

    @Override
    // GROOVY-8707
    void testPlusEqualsOnProperty() {
        super.testPlusEqualsOnProperty()
        String bytecode = astTrees['C'][1]
        assert !bytecode.contains('ScriptBytecodeAdapter.setGroovyObjectProperty'): '"c.i += 10" should use setter, not dynamic property'
    }
}
