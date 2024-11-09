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
package groovy.bugs;

import org.junit.Test;

import static groovy.test.GroovyAssert.assertScript;

public final class Groovy6653 {

    @Test
    public void testCovariantMethodSuperCall() throws Exception {
        assertScript(
            "class D extends " + C.class.getName() + " {\n" +
                "    @Override\n" +
                "    protected String getText(String s) {\n" +
                "        super.getText(s) + 'D'\n" +
                "    }\n" +
                "}\n" +
                "String result = new D().getText(null)\n" +
                "assert result == 'ACD'\n"
        );
    }

    public static abstract class A<T> {
        protected String getText(T t) {
            return "A";
        }
    }

    public static class C extends A<String> {
        @Override
        protected String getText(String s) {
            return super.getText(s) + "C";
        }
    }
}
