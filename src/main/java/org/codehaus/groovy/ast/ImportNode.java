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
package org.codehaus.groovy.ast;

import java.util.Objects;

/**
 * Represents an import statement.
 */
public class ImportNode extends AnnotatedNode {

    private final String alias;
    private final String fieldName;
    private final String packageName;
    private final boolean isStar;
    private final boolean isStatic;
    private ClassNode type;
    private transient int hashCode;

    /**
     * An import of a single type, i.e.&#160;{@code import pack.Type} or {@code import pack.Type as Alias}
     *
     * @param type  the type reference
     * @param alias optional alias
     */
    public ImportNode(final ClassNode type, final String alias) {
        this.type = Objects.requireNonNull(type);
        this.alias = alias;
        this.isStar = false;
        this.isStatic = false;
        this.packageName = null;
        this.fieldName = null;
    }

    /**
     * An import of all types in a package, i.e.&#160;{@code import pack.*}
     *
     * @param packageName the name of the package
     */
    public ImportNode(final String packageName) {
        this.type = null;
        this.alias = null;
        this.isStar = true;
        this.isStatic = false;
        this.packageName = Objects.requireNonNull(packageName);
        this.fieldName = null;
    }

    /**
     * An import of all static members of a type, i.e.&#160;{@code import static pack.Type.*}
     *
     * @param type the type reference
     */
    public ImportNode(final ClassNode type) {
        this.type = Objects.requireNonNull(type);
        this.alias = null;
        this.isStar = true;
        this.isStatic = true;
        this.packageName = null;
        this.fieldName = null;
    }

    /**
     * An import of a static field or method of a type, i.e.&#160;{@code import static pack.Type.name} or {@code import static pack.Type.name as alias}
     *
     * @param type      the type reference
     * @param fieldName the field name
     * @param alias     optional alias
     */
    public ImportNode(final ClassNode type, final String fieldName, final String alias) {
        this.type = Objects.requireNonNull(type);
        this.alias = alias;
        this.isStar = false;
        this.isStatic = true;
        this.packageName = null;
        this.fieldName = Objects.requireNonNull(fieldName);
    }

    /**
     * @return the text display of this import
     */
    @Override
    public String getText() {
        String simpleName = getAlias();
        String memberName = getFieldName();

        if (!isStatic()) {
            if (isStar()) {
                return "import " + getPackageName() + "*";
            } else if (simpleName == null || simpleName.isEmpty()
                || simpleName.equals(getType().getNameWithoutPackage())) {
                return "import " + getClassName();
            } else {
                return "import " + getClassName() + " as " + simpleName;
            }
        } else {
            if (isStar()) {
                return "import static " + getClassName() + ".*";
            } else if (simpleName == null || simpleName.isEmpty() || simpleName.equals(memberName)) {
                return "import static " + getClassName() + "." + memberName;
            } else {
                return "import static " + getClassName() + "." + memberName + " as " + simpleName;
            }
        }
    }

    public boolean isStar() {
        return isStar;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getAlias() {
        return alias;
    }

    public String getClassName() {
        return (type == null ? null : type.getName());
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getPackageName() {
        return packageName;
    }

    public ClassNode getType() {
        return type;
    }

    public void setType(final ClassNode type) {
        this.type = Objects.requireNonNull(type);
        hashCode = 0;
    }

    @Override
    public boolean equals(Object that) {
        if (that == this) return true;
        if (!(that instanceof ImportNode)) return false;

        ImportNode node = (ImportNode) that;
        if (!Objects.equals(type, node.type))
            return false;
        if (!Objects.equals(alias, node.alias))
            return false;
        if (!Objects.equals(fieldName, node.fieldName))
            return false;
        if (!Objects.equals(packageName, node.packageName))
            return false;
        return (isStar == node.isStar && isStatic == node.isStatic);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            hashCode = Objects.hash(type, alias, fieldName, packageName, isStar, isStatic);
        }
        return result;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
    }
}
