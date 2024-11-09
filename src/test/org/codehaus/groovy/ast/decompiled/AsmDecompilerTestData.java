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
package org.codehaus.groovy.ast.decompiled;

import org.codehaus.groovy.ast.ClassNode;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

enum SomeEnum {FOO, BAR}

interface Intf<S> {
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@interface Anno {
    String stringAttr() default "";

    SomeEnum enumAttr() default SomeEnum.FOO;

    Class clsAttr() default Object.class;

    boolean booleanAttr() default true;

    int[] intArrayAttr() default {};

    Class[] classArrayAttr() default {};

    InnerAnno[] annoArrayAttr() default {};
}

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@interface TypeAnno {
}

@Retention(RetentionPolicy.RUNTIME)
@interface InnerAnno {
    boolean booleanAttr() default true;
}

@SuppressWarnings("unused")
@Anno(
    stringAttr = "s",
    enumAttr = SomeEnum.BAR,
    intArrayAttr = {4, 2},
    clsAttr = String.class,
    classArrayAttr = {AsmDecompilerTestData.class},
    annoArrayAttr = {@InnerAnno, @InnerAnno(booleanAttr = false)}
)
public class AsmDecompilerTestData<T extends List<? super T>, V> extends SuperClass implements Intf<Map<T, String>> {
    public List<T> genericField;
    @Anno
    protected Object aField;

    public AsmDecompilerTestData(boolean b) {
    }

    static <T extends List<? super T>> AsmDecompilerTestData<T, Integer>.Inner<String> returnInner() {
        return null;
    }

    @Anno
    public ClassNode objectMethod() {
        return null;
    }

    public void withParametersThrowing(@Anno int a, AsmDecompilerTestData[] b) throws IOException {
    }

    public int[][] primitiveArrayMethod() {
        return null;
    }

    public <A extends Number, B extends IOException> List<?> genericMethod(A a, int[] array) throws B {
        return null;
    }

    public <A, B extends IOException> List<?> nonGenericExceptions(A a, int[] array) throws IOException {
        return null;
    }

    public <A, B extends IOException> List<?> nonGenericParameters(boolean b) throws B {
        return null;
    }

    public List<?> nonParameterizedGenerics() {
        return null;
    }

    static class InnerStatic {
    }

    static class Inner$WithDollar {
    }

    class Inner<X> {
    }
}

class SuperClass {
}

@SuppressWarnings("unused")
abstract class NonTrivialErasure<V extends RuntimeException> {
    V field;

    abstract V method(V param) throws V;
}

@SuppressWarnings("unused")
abstract class WithTypeAnnotations extends @TypeAnno Object implements @TypeAnno Cloneable, @TypeAnno List<@TypeAnno Object> {
    @TypeAnno
    Object field;

    @TypeAnno
    WithTypeAnnotations() {
    }

    @TypeAnno
    Object method(@TypeAnno Object param) throws @TypeAnno Exception {
        return null;
    }

    <T extends @TypeAnno Object> @TypeAnno T parameterizedMethod() {
        return null;
    }
}
