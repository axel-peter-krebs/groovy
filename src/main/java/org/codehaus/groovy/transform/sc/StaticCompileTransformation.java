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
package org.codehaus.groovy.transform.sc;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesWriterControllerFactoryImpl;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.sc.transformers.StaticCompilationTransformer;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;

import java.util.Collections;
import java.util.Map;

import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.STATIC_COMPILE_NODE;

/**
 * Handles the implementation of the {@link groovy.transform.CompileStatic} transformation.
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class StaticCompileTransformation extends StaticTypesTransformation {

    private final StaticTypesWriterControllerFactoryImpl factory = new StaticTypesWriterControllerFactoryImpl();

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        AnnotatedNode target = (AnnotatedNode) nodes[1];
        if (Boolean.TRUE.equals(target.getNodeMetaData(StaticTypeCheckingVisitor.class))) {
            return;
        }
        Map<String, Expression> members = ((AnnotationNode) nodes[0]).getMembers();
        Expression extensions = members.get("extensions");
        StaticTypeCheckingVisitor visitor = null;
        if (target instanceof ClassNode) {
            ClassNode classNode = (ClassNode) target;
            visitor = newVisitor(source, classNode);
            visitor.setCompilationUnit(compilationUnit);
            addTypeCheckingExtensions(visitor, extensions);
            classNode.putNodeMetaData(WriterControllerFactory.class, factory);
            target.putNodeMetaData(STATIC_COMPILE_NODE, !visitor.isSkipMode(target));
            visitor.initialize();
            visitor.visitClass(classNode);
        } else if (target instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) target;
            ClassNode declaringClass = methodNode.getDeclaringClass();
            visitor = newVisitor(source, declaringClass);
            visitor.setCompilationUnit(compilationUnit);
            addTypeCheckingExtensions(visitor, extensions);
            methodNode.putNodeMetaData(STATIC_COMPILE_NODE, !visitor.isSkipMode(target));
            if (declaringClass.getNodeMetaData(WriterControllerFactory.class) == null) {
                declaringClass.putNodeMetaData(WriterControllerFactory.class, factory);
            }
            visitor.setMethodsToBeVisited(Collections.singleton(methodNode));
            visitor.initialize();
            visitor.visitMethod(methodNode);
        } else if (!(target instanceof PropertyNode)) {
            source.addError(new SyntaxException(STATIC_ERROR_PREFIX + "Unimplemented node type", target));
        }
        if (visitor != null) {
            visitor.performSecondPass();
        }
        StaticCompilationTransformer transformer = new StaticCompilationTransformer(source, visitor);
        if (target instanceof ClassNode) {
            transformer.visitClass((ClassNode) target);
        } else if (target instanceof MethodNode) {
            transformer.visitMethod((MethodNode) target);
        }
    }

    @Override
    protected StaticTypeCheckingVisitor newVisitor(final SourceUnit unit, final ClassNode node) {
        return new StaticCompilationVisitor(unit, node);
    }
}
