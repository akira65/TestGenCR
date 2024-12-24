package org.example.testgen_cr.model_buffs;

import org.eclipse.jdt.core.dom.*;
import org.jtool.cfg.CFG;
import org.jtool.cfg.CFGMethodCall;
import org.jtool.cfg.CFGNode;
import org.jtool.jxplatform.builder.ModelBuilder;
import org.jtool.srcmodel.JavaClass;
import org.jtool.srcmodel.JavaMethod;
import org.jtool.srcmodel.JavaProject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodFinder {
    private ModelBuilder builder;
    private List<TestGenMethodSeq> testMethodSeqList = new ArrayList<>();

    public MethodFinder(ModelBuilder builder) {
        this.builder = builder;
    }

    public List<TestGenMethodSeq> collectAllMethodSeq(JavaProject jproject, int loc, int callChainNum) {
        List<TestGenMethodSeq> allSeq = new ArrayList<>();

        jproject.getClasses().forEach(jc -> {
            jc.getMethods().forEach(jm -> {
                List<TestGenMethodSeq> seqs = collectCallingMethodSeqList(jm, callChainNum);
                for (TestGenMethodSeq seq : seqs) {
                    if (getLoc(seq.getMethodInTestClass().getJavaMethod()) > loc) {
                        allSeq.add(seq);
                    }
                    ;
                }
                ;
            });
        });

        return allSeq;
    }


    private static int getLoc(JavaMethod jmethod) {
        if (jmethod.getASTNode() instanceof MethodDeclaration) {
            MethodDeclaration methodDecl = (MethodDeclaration) jmethod.getASTNode();
            CompilationUnit cu = (CompilationUnit) methodDecl.getRoot();

            int startPosition = methodDecl.getStartPosition();
            int endPosition = methodDecl.getStartPosition() + methodDecl.getLength() - 1;
            int upperLineNumber = cu.getLineNumber(startPosition);
            int bottomLineNumber = cu.getLineNumber(endPosition);

            int docupperLineNumber = 0;
            int docbottomLineNumber = 0;
            Javadoc javadoc = methodDecl.getJavadoc();
            if (javadoc != null) {
                int docstartPosition = javadoc.getStartPosition();
                int docendPosition = javadoc.getStartPosition() + javadoc.getLength() - 1;
                docupperLineNumber = cu.getLineNumber(docstartPosition);
                docbottomLineNumber = cu.getLineNumber(docendPosition);
            }

            int loc = (bottomLineNumber - upperLineNumber) - (docbottomLineNumber - docupperLineNumber);

            return loc;
        }
        return 0;
    }

    public List<TestGenMethodSeq> collectCallingMethodSeqList(JavaMethod jmethod, int callChainNum) {
        Set<TestGenMethodSeq> seqs = new HashSet<>();

        ITypeBinding typebinding = jmethod.getDeclaringClass().getTypeBinding();

        if (typebinding.isTopLevel()) {


            for (JavaMethod jm : jmethod.getCallingMethodsInProject()) {
//                callerがテストメソッドじゃない
                if (!MethodFinder.isTestMethod(jm)) {
                    for (TestGenMethod tm : getCallingMethods(jm, jmethod)) {
                        collectCallingMethods(seqs, tm, new ArrayList<>(), callChainNum + 1);
                    }
                }
            }
        }
        for (TestGenMethodSeq testMethodSeq : seqs) {
            testMethodSeqList.add(testMethodSeq);
        }
        return testMethodSeqList;
    }

    private void collectCallingMethods(Set<TestGenMethodSeq> seqs,
                                       TestGenMethod tmethod, List<TestGenMethod> callingMethods, int count) {
        if (count == 0 || callingMethods.contains(tmethod)) {
            return;
        }

        if (tmethod.getJavaClass().getTypeBinding() == null ||
                !tmethod.getJavaClass().getTypeBinding().isTopLevel()) {
            return;
        }

        if (tmethod.getJavaMethod().isConstructor()) {
            return;
        }

        callingMethods.add(tmethod);
        JavaMethod jmethod = tmethod.getJavaMethod();

        if (MethodFinder.isTestMethod(jmethod)) {
            if (!jmethod.isStatic()) {
                TestGenMethodSeq testMethodSeq = new TestGenMethodSeq(callingMethods, tmethod);
                seqs.add(testMethodSeq);
            }
            return;
        }

        for (JavaMethod jm : jmethod.getCallingMethodsInProject()) {
            for (TestGenMethod tm : getCallingMethods(jm, jmethod)) {
                collectCallingMethods(seqs, tm, new ArrayList<>(callingMethods), count - 1);
            }
        }
    }

    private Set<TestGenMethod> getCallingMethods(JavaMethod caller, JavaMethod callee) {
        Set<TestGenMethod> methods = new HashSet<>();
        CFG cfg = builder.getCFG(caller);
        for (CFGNode node : cfg.getNodes()) {
            if (node.isMethodCall()) {
                CFGMethodCall callNode = (CFGMethodCall) node;
                if (callNode.getQualifiedName().equals(callee.getQualifiedName())) {
                    methods.add(new TestGenMethod(cfg, caller, callNode));
                }
            }
        }
        return methods;
    }

    public static boolean isTestMethod(JavaMethod jmethod) {
        if (jmethod.getDeclaringClass() == null) {
            return false;
        }

        JavaClass jclass = jmethod.getDeclaringClass().getSuperClass();

        if (jclass != null) {
            if (jclass.getQualifiedName().fqn().equals("junit.framework.TestCase")) {
                return true;
            }
        }

        String anno = MethodFinder.getAnnotation(jmethod);

        return "Test".equals(anno);
    }

    public static String getAnnotation(JavaMethod jmethod) {
        ASTNode node = jmethod.getASTNode();
        if (node instanceof MethodDeclaration methodDecl) {
            for (Object obj : methodDecl.modifiers()) {
                IExtendedModifier mod = (IExtendedModifier) obj;
                if (mod.isAnnotation()) {
                    Annotation anno = (Annotation) mod;
                    Name name = anno.getTypeName();
                    return name.getFullyQualifiedName();

                }
            }
        }
        return "";
    }
}
