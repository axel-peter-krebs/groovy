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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.codehaus.groovy.ast.tools.GeneralUtils.defaultValueX;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;

/**
 * Utility class to add return statements.
 * <p>
 * Extracted from Verifier as it can be useful for some AST transformations.
 */
public class ReturnAdder {

    private static final ReturnStatementListener DEFAULT_LISTENER = returnStatement -> {
    };
    private final ReturnStatementListener listener;
    /**
     * If set to 'true', then returns are effectively added. This is useful whenever you just want
     * to check what returns are produced without eventually adding them.
     */
    private final boolean doAdd;

    public ReturnAdder() {
        this.listener = DEFAULT_LISTENER;
        this.doAdd = true;
    }

    public ReturnAdder(final ReturnStatementListener listener) {
        this.listener = Objects.requireNonNull(listener);
        this.doAdd = false;
    }

    /**
     * @deprecated Use {@link #visitMethod(MethodNode)} instead.
     */
    @Deprecated
    public static void addReturnIfNeeded(final MethodNode node) {
        new ReturnAdder().visitMethod(node);
    }

    /**
     * Adds return statements to given method whenever an implicit return is detected.
     */
    public void visitMethod(final MethodNode node) {
        if (!node.isVoidMethod()) {
            Statement code = node.getCode();
            if (code != null) { // happens with @interface methods
                code = addReturnsIfNeeded(code, node.getReturnType(), node.getVariableScope());
                if (doAdd) node.setCode(code);
            }
        }
    }

    private Statement addReturnsIfNeeded(final Statement statement, final ClassNode rtype, final VariableScope scope) {
        if (statement instanceof ReturnStatement || statement instanceof ThrowStatement
            || statement instanceof EmptyStatement || statement instanceof BytecodeSequence) {
            return statement;
        }

        if (statement == null) {
            ReturnStatement returnStatement = new ReturnStatement(defaultValueX(rtype));
            listener.returnStatementAdded(returnStatement);
            return returnStatement;
        }

        if (statement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) statement).getExpression();
            ReturnStatement returnStatement = new ReturnStatement(expression);
            returnStatement.copyStatementLabels(statement);
            returnStatement.setSourcePosition(statement.getLineNumber() < 0 ? expression : statement);
            listener.returnStatementAdded(returnStatement);
            return returnStatement;
        }

        if (statement instanceof SynchronizedStatement) {
            SynchronizedStatement syncStatement = (SynchronizedStatement) statement;
            Statement code = addReturnsIfNeeded(syncStatement.getCode(), rtype, scope);
            if (doAdd) syncStatement.setCode(code);
            return syncStatement;
        }

        if (statement instanceof IfStatement) {
            IfStatement ifElseStatement = (IfStatement) statement;
            Statement ifBlock = addReturnsIfNeeded(ifElseStatement.getIfBlock(), rtype, scope);
            Statement elseBlock = addReturnsIfNeeded(ifElseStatement.getElseBlock(), rtype, scope);
            if (doAdd) {
                ifElseStatement.setIfBlock(ifBlock);
                ifElseStatement.setElseBlock(elseBlock);
            }
            return ifElseStatement;
        }

        if (statement instanceof SwitchStatement) {
            SwitchStatement switchStatement = (SwitchStatement) statement;
            Statement defaultStatement = switchStatement.getDefaultStatement();
            List<CaseStatement> caseStatements = switchStatement.getCaseStatements();
            for (Iterator<CaseStatement> it = caseStatements.iterator(); it.hasNext(); ) {
                CaseStatement caseStatement = it.next();
                Statement code = adjustSwitchCaseCode(caseStatement.getCode(), rtype, scope,
                    // GROOVY-9896: return if no default and last case lacks break
                    defaultStatement == EmptyStatement.INSTANCE && !it.hasNext());
                if (doAdd) caseStatement.setCode(code);
            }
            defaultStatement = adjustSwitchCaseCode(defaultStatement, rtype, scope, true);
            if (doAdd) switchStatement.setDefaultStatement(defaultStatement);
            return switchStatement;
        }

        if (statement instanceof TryCatchStatement) {
            TryCatchStatement tryCatchFinally = (TryCatchStatement) statement;
            boolean[] missesReturn = new boolean[1];
            new ReturnAdder(returnStatement -> missesReturn[0] = true)
                .addReturnsIfNeeded(tryCatchFinally.getFinallyStatement(), rtype, scope);
            boolean hasFinally = !(tryCatchFinally.getFinallyStatement() instanceof EmptyStatement);

            // if there is no missing return in the finally block and the block exists
            // there is nothing to do
            if (hasFinally && !missesReturn[0]) return tryCatchFinally;

            // add returns to try and catch blocks
            Statement tryStatement = addReturnsIfNeeded(tryCatchFinally.getTryStatement(), rtype, scope);
            if (doAdd) tryCatchFinally.setTryStatement(tryStatement);
            for (CatchStatement catchStatement : tryCatchFinally.getCatchStatements()) {
                Statement code = addReturnsIfNeeded(catchStatement.getCode(), rtype, scope);
                if (doAdd) catchStatement.setCode(code);
            }
            return tryCatchFinally;
        }

        if (statement instanceof BlockStatement) {
            BlockStatement blockStatement = (BlockStatement) statement;
            if (blockStatement.isEmpty()) {
                ReturnStatement returnStatement = new ReturnStatement(defaultValueX(rtype));
                returnStatement.copyStatementLabels(blockStatement);
                returnStatement.setSourcePosition(blockStatement);
                listener.returnStatementAdded(returnStatement);
                return returnStatement;
            } else {
                List<Statement> statements = blockStatement.getStatements();
                int lastIndex = statements.size() - 1;
                Statement last = addReturnsIfNeeded(statements.get(lastIndex), rtype, blockStatement.getVariableScope());
                if (doAdd) statements.set(lastIndex, last);
                return blockStatement;
            }
        }

        List<Statement> statements = new ArrayList<>(2);
        statements.add(statement);

        ReturnStatement returnStatement = new ReturnStatement(defaultValueX(rtype));
        listener.returnStatementAdded(returnStatement);
        statements.add(returnStatement);

        BlockStatement blockStatement = new BlockStatement(statements, new VariableScope(scope));
        blockStatement.setSourcePosition(statement);
        return blockStatement;
    }

    private Statement adjustSwitchCaseCode(final Statement statement, final ClassNode rtype, final VariableScope scope, final boolean lastCase) {
        if (!statement.isEmpty() && statement instanceof BlockStatement) {
            BlockStatement block = (BlockStatement) statement;
            int breakIndex = block.getStatements().size() - 1;
            if (block.getStatements().get(breakIndex) instanceof BreakStatement) {
                if (doAdd) {
                    Statement breakStatement = block.getStatements().remove(breakIndex);
                    if (breakIndex == 0) block.addStatement(EmptyStatement.INSTANCE);
                    addReturnsIfNeeded(block, rtype, scope);
                    // GROOVY-9880: some code structures will fall through
                    Statement lastStatement = last(block.getStatements());
                    if (!(lastStatement instanceof ReturnStatement
                        || lastStatement instanceof ThrowStatement)) {
                        block.addStatement(breakStatement);
                    }
                } else {
                    addReturnsIfNeeded(new BlockStatement(block.getStatements().subList(0, breakIndex), null), rtype, scope);
                }
            } else if (lastCase) {
                return addReturnsIfNeeded(statement, rtype, scope);
            }
        }
        return statement;
    }

    @FunctionalInterface
    public interface ReturnStatementListener {
        /**
         * Implement this method in order to be notified whenever a return statement is generated.
         */
        void returnStatementAdded(ReturnStatement returnStatement);
    }
}
