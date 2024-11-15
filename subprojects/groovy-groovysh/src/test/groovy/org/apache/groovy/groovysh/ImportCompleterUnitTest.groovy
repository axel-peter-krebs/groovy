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
package org.apache.groovy.groovysh

import groovy.mock.interceptor.MockFor
import groovy.test.GroovyTestCase
import org.apache.groovy.groovysh.commands.ImportCompleter
import org.apache.groovy.groovysh.util.PackageHelper
import org.codehaus.groovy.tools.shell.util.Preferences

class ImportCompleterUnitTest extends GroovyTestCase {

    private MockFor preferencesMocker


    @Override
    void setUp() {
        super.setUp()
        preferencesMocker = new MockFor(Preferences)
    }

    void testPatternPackOrClassname() {
        assert ''.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'j'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'java.'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'java.util'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'java.util.'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'java.util.T'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'org.w3c.T'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'java.util.Test'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'java.util.Test123'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'java.util.Test$foo123'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert 'java.util.Test_foo123'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        // inverse
        assert !'.'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert !'Upper'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert !'java.util.Test123.'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
        assert !'java.util.Test123.foo'.matches(ImportCompleter.PACK_OR_CLASSNAME_PATTERN)
    }

    void testPatternPackOrSimpleClassname() {
        assert ''.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert 'j'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert 'java.'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert 'java.util'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert 'java.util.'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert 'java.util.T'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert 'org.w3c.T'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert 'java.util.Test'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert 'java.util.Test123'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        // inverse
        assert !'.'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert !'Upper'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert !'java.util.Test123.'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
        assert !'java.util.Test123.foo'.matches(ImportCompleter.PACK_OR_SIMPLE_CLASSNAME_PATTERN)
    }

    void testPatternClassOrMethodName() {
        assert ''.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'j'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'java.'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'java.util'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'java.util.'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'java.util.T'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'org.w3c.T'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'java.util.Test'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'java.util.Test123'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'java.util.Test123.'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert 'java.util.Test123.foo'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        //inverse
        assert !'.'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert !'Upper'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert !'java.util.Test123.foo.'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
        assert !'java.util.Test123.Test.foo'.matches(ImportCompleter.PACK_OR_CLASS_OR_METHODNAME_PATTERN)
    }

    void testPatternQualifiedClassDot() {
        assert !''.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'j'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.util'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.util.'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.util.T'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'org.w3c.T'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.util.Test'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.util.Test123'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert 'java.util.Test123.'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.util.Test123.foo'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'.'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'Upper'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.util.Test123.foo.'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
        assert !'java.util.Test123.Test.foo'.matches(ImportCompleter.QUALIFIED_CLASS_DOT_PATTERN)
    }

    void testLowercaseImportItem() {
        assert !''.matches(ImportCompleter.LOWERCASE_IMPORT_ITEM_PATTERN)
        assert 'j'.matches(ImportCompleter.LOWERCASE_IMPORT_ITEM_PATTERN)
        assert 'java.'.matches(ImportCompleter.LOWERCASE_IMPORT_ITEM_PATTERN)
        assert 'java.util'.matches(ImportCompleter.LOWERCASE_IMPORT_ITEM_PATTERN)
        assert 'java.util.'.matches(ImportCompleter.LOWERCASE_IMPORT_ITEM_PATTERN)
        assert !'java.util.*'.matches(ImportCompleter.LOWERCASE_IMPORT_ITEM_PATTERN)
        assert !'java.util.T'.matches(ImportCompleter.LOWERCASE_IMPORT_ITEM_PATTERN)
    }


    private void assertCompletionCandidatesMatch(
        final PackageHelper packageHelper,
        final String buffer,
        final boolean staticImport,
        final List<String> expected) {
        ImportCompleter compl = new ImportCompleter(packageHelper, null, staticImport)
        def candidates = []

        assert (buffer.lastIndexOf('.') + 1) == compl.complete(buffer, buffer.length(), candidates)
        assert expected == candidates.sort()
    }

    void testCompleteEmpty() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['java', 'groovy']), '', false, ['groovy.', 'java.'])
        }
    }

    void testCompleteStaticEmpty() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['java', 'groovy']), '', true, ['groovy.', 'java.'])
        }
    }

    void testCompleteJ() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['java', 'javax']), 'j', false, ['java.', 'javax.'])
        }
    }

    void testCompleteStaticJ() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['java', 'javax']), 'j', true, ['java.', 'javax.'])
        }
    }

    void testCompleteCo() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['com', 'org']), 'co', false, ['com.'])
        }
    }

    void testCompleteJavaDot() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['util', 'math']), 'java.', false, ['* ', 'math.', 'util.'])
        }
    }

    void testCompleteStaticJavaDot() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['util', 'math']), 'java.', true, ['math.', 'util.'])
        }
    }

    void testCompleteJavaDotU() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['util', 'math']), 'java.u', false, ['util.'])
        }
    }

    void testCompleteJavaDotUtil() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['util']), 'java.util', false, ['util.'])
        }
    }

    void testCompleteJavaDotUtilDot() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['zip', 'jar']), 'java.util.', false, ['* ', 'jar.', 'zip.'])
        }
    }

    void testCompleteJavaDotUtilDotZip() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['zip']), 'java.util.zip', false, ['zip.'])
        }
    }

    void testCompleteJavaDotUtilDotZipDot() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['Test1', 'Test2']), 'java.util.zip.', false, ['* ', 'Test1 ', 'Test2 '])
        }
    }

    void testCompleteStaticJavaDotUtilDotZipDot() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['Test1', 'Test2']), 'java.util.zip.', true, ['Test1.', 'Test2.'])
        }
    }

    void testCompleteJavaDotUtilDotZipDotT() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['Test', 'NotThis']), 'java.util.zip.T', false, ['Test '])
        }
    }

    void testCompleteStaticJavaDotUtilDotZipDotT() {
        preferencesMocker.use {
            assertCompletionCandidatesMatch(new MockPackageHelper(['Test', 'NotThis']), 'java.util.zip.T', true, ['Test '])
        }
    }

    void testCompleteStaticJavaDotUtilDotZipDotTestDot() {
        preferencesMocker.use {
            def evaluator = new Evaluator() {
                @Override
                def evaluate(Collection<String> buffer) {
                    assert (buffer == ['java.util.zip.Test'])
                    return Math
                }
            }
            ImportCompleter compl = new ImportCompleter(new MockPackageHelper([]), evaluator, true)
            String buffer = 'java.util.zip.Test.'
            def candidates = ['previousitem']
            assert (buffer.lastIndexOf('.') + 1) == compl.complete(buffer, buffer.length(), candidates)
            assert '* ' in candidates
            assert candidates.toString(), 'abs ' in candidates
            assert 'previousitem' in candidates
        }
    }

    void testCompleteStaticJavaDotUtilDotZipDotTestDotMa() {
        preferencesMocker.use {
            def evaluator = new Evaluator() {
                @Override
                def evaluate(Collection<String> buffer) {
                    assert (buffer == ['java.util.zip.Test'])
                    return Math
                }
            }
            ImportCompleter compl = new ImportCompleter(new MockPackageHelper([]), evaluator, true)
            def candidates = []
            String buffer = 'java.util.zip.Test.ma'
            assert 19 == compl.complete(buffer, buffer.length(), candidates)
            assert ['max '] == candidates.sort()
        }
    }
}
