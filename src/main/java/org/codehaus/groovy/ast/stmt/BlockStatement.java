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
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.VariableScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * A list of statements and a scope.
 */
public class BlockStatement extends Statement {

    private List<Statement> statements;
    private VariableScope scope;

    public BlockStatement() {
        this(new ArrayList<>(), new VariableScope());
    }

    /**
     * Creates a BlockStatement with a scope and children statements.
     *
     * @param statements the statements, which cannot be null or an exception occurs. No reference
     *                   to the array is held, so modifying the array later has no effect on this
     *                   class.
     * @param scope      the scope
     */
    public BlockStatement(final Statement[] statements, final VariableScope scope) {
        this(new ArrayList<>(Arrays.asList(statements)), scope);
    }

    /**
     * Creates a BlockStatement with a scope and children statements.
     *
     * @param statements the statements. Do not pass null. If you do, no exception will occur,
     *                   but a NullPointerException will eventually occur later. Also, a reference
     *                   to the list is kept, so modifying the List later does effect this class.
     * @param scope      the scope
     */
    public BlockStatement(final List<Statement> statements, final VariableScope scope) {
        this.statements = statements;
        this.scope = scope;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitBlockStatement(this);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void addStatement(final Statement statement) {
        statements.add(statement);
    }

    public void addStatements(final List<Statement> listOfStatements) {
        statements.addAll(listOfStatements);
    }

    @Override
    public String getText() {
        StringJoiner text = new StringJoiner("; ", "{ ", " }");
        for (Statement statement : statements) {
            text.add(statement.getText());
        }
        return text.toString();
    }

    @Override
    public String toString() {
        return super.toString() + statements;
    }

    @Override
    public boolean isEmpty() {
        return statements.isEmpty();
    }

    public VariableScope getVariableScope() {
        return scope;
    }

    public void setVariableScope(final VariableScope scope) {
        this.scope = scope;
    }
}
