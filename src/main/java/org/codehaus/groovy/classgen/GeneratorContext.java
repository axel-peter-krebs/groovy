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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.MethodNode;

/**
 * A context shared across generations of a class and its inner classes.
 */
public class GeneratorContext {

    private static final boolean[] CHARACTERS_TO_ENCODE;
    private static final int MIN_ENCODING, MAX_ENCODING;

    static {
        char[] chars = {' ', '!', '"', '#', '$', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '@', '[', '\\', ']', '^', '{', '}', '~'};

        MIN_ENCODING = chars[0];
        MAX_ENCODING = chars[chars.length - 1];
        CHARACTERS_TO_ENCODE = new boolean[MAX_ENCODING - MIN_ENCODING + 1];

        for (char c : chars) {
            CHARACTERS_TO_ENCODE[c - MIN_ENCODING] = true;
        }
    }

    private final CompileUnit compileUnit;
    private int innerClassIdx = 1;
    private int closureClassIdx = 1;
    private int syntheticMethodIdx = 0;

    public GeneratorContext(final CompileUnit compileUnit) {
        this.compileUnit = compileUnit;
    }

    public GeneratorContext(final CompileUnit compileUnit, final int innerClassOffset) {
        this.compileUnit = compileUnit;
        this.innerClassIdx = innerClassOffset;
    }

    public static String encodeAsValidClassName(final String name) {
        if ("module-info".equals(name) || "package-info".equals(name)) return name;

        int lastEscape = -1;
        StringBuilder b = null;
        final int n = name.length();
        for (int i = 0; i < n; i += 1) {
            int encodeIndex = name.charAt(i) - MIN_ENCODING;
            if (encodeIndex >= 0 && encodeIndex < CHARACTERS_TO_ENCODE.length) {
                if (CHARACTERS_TO_ENCODE[encodeIndex]) {
                    if (b == null) {
                        b = new StringBuilder(name.length() + 3);
                        b.append(name, 0, i);
                    } else {
                        b.append(name, lastEscape + 1, i);
                    }
                    b.append('_');
                    lastEscape = i;
                }
            }
        }
        if (b == null) return name;
        if (lastEscape == -1) throw new GroovyBugError("unexpected escape char control flow in " + name);
        b.append(name, lastEscape + 1, n);
        return b.toString();
    }

    // for ACG nestmate determination !
    int getClosureClassIndex() {
        return closureClassIdx;
    }

    public int getNextInnerClassIdx() {
        return innerClassIdx++;
    }

    public CompileUnit getCompileUnit() {
        return compileUnit;
    }

    public String getNextClosureInnerName(final ClassNode owner, final ClassNode enclosingClass, final MethodNode enclosingMethod) {
        return getNextInnerName(enclosingClass, enclosingMethod, "closure");
    }

    public String getNextLambdaInnerName(final ClassNode owner, final ClassNode enclosingClass, final MethodNode enclosingMethod) {
        return getNextInnerName(enclosingClass, enclosingMethod, "lambda");
    }

    private String getNextInnerName(final ClassNode enclosingClass, final MethodNode enclosingMethod, final String classifier) {
        String typeName = "_" + classifier + closureClassIdx++;
        if (enclosingMethod != null && !ClassHelper.isGeneratedFunction(enclosingClass)) {
            typeName = "_" + encodeAsValidClassName(enclosingMethod.getName()) + typeName;
        }
        return typeName;
    }

    public String getNextConstructorReferenceSyntheticMethodName(final MethodNode enclosingMethodNode) {
        return "ctorRef$"
            + (null == enclosingMethodNode
            ? ""
            : enclosingMethodNode.getName().replace("<", "").replace(">", "") + "$")
            + syntheticMethodIdx++;
    }
}
