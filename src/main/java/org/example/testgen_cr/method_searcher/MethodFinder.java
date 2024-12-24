package org.example.testgen_cr.method_searcher;

import org.jtool.srcmodel.JavaProject;
import org.jtool.srcmodel.JavaClass;
import org.jtool.srcmodel.JavaMethod;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Name;

import java.util.List;
import java.util.ArrayList;

public class MethodFinder {

    private static final boolean nonPublicFlag = true;

    private final List<JavaMethod> allMethods = new ArrayList<JavaMethod>();
    private final List<MethodSeq> methodSeqList = new ArrayList<MethodSeq>();
    private final List<MethodSeq> targetMethodSeqList = new ArrayList<MethodSeq>();
    private final List<JavaClass> allClasses = new ArrayList<JavaClass>();
    private int allMethodLoc = 0;


    public void run(JavaProject jproject, int loc, int callChainNum) {
        for (JavaClass jc : jproject.getClasses()) {
            for (JavaMethod jm : jc.getMethods()) {
                if (!MethodFinder.isTestMethod(jm)){
                    allMethods.add(jm);
                }
            }
        }

        List<JavaMethod> methodList = new ArrayList<JavaMethod>();
        for (JavaMethod jm : allMethods) {
            collectTargetCandidates(jm, methodList, callChainNum - 1);
        }

        for (JavaMethod jmethod : methodList) {
            collectCallingMethodSeqList(jmethod, new ArrayList<JavaMethod>(), callChainNum);
        }

        for (MethodSeq seq : methodSeqList) {
            int loc1 = MethodFinder.getLoc(seq.callee());
            if (loc1 > loc) {
                targetMethodSeqList.add(seq);
            }
        }
    }

    public int getAllMethodLoc() {
        for (JavaMethod jm : allMethods) {
            int loc = MethodFinder.getLoc(jm);
            allMethodLoc = allMethodLoc + loc;
        }
        return allMethodLoc;
    }

    public int getClassNum(JavaProject jproject) {
        for (JavaMethod jm : allMethods) {
            if (!allClasses.contains(jm.getDeclaringClass())) {
                allClasses.add(jm.getDeclaringClass());
            }
        }
        return allClasses.size();
    }

    public List<JavaMethod> getAllMethods() {
        return allMethods;
    }

    public List<MethodSeq> getTargetMethodSeqList() {
        return targetMethodSeqList;
    }

    private boolean isTarget(JavaMethod jmethod) {
        return !MethodFinder.isTestMethod(jmethod) &&
                !jmethod.isConstructor() &&
                jmethod.getDeclaringClass().getTypeBinding().isTopLevel() &&
                jmethod.isInProject() &&
                (jmethod.isPublic() || nonPublicFlag);
    }


    private void collectTargetCandidates(JavaMethod jmethod, List<JavaMethod> calledMethods, int count) {
        for (JavaMethod jm : jmethod.getCalledMethodsInProject()) {
            if (count == 0) {
                if (isTarget(jm)) {
                    if (!calledMethods.contains(jm)) {
                        calledMethods.add(jm);
                    }
                }
            } else {
                collectTargetCandidates(jm, calledMethods, count - 1);
            }
        }
    }

    private static boolean isTestMethod(JavaMethod jmethod) {

        JavaClass jclass = jmethod.getDeclaringClass().getSuperClass();
        if (jclass != null) {
            if (jclass.getQualifiedName().fqn().equals("junit.framework.TestCase")) {
                return true;
            }
        }

        if (jmethod.getClassName().matches(".*Test$")) {
            return true;
        }
        String anno = MethodFinder.getAnnotation(jmethod);

        return "Test".equals(anno);
    }

    private void collectCallingMethodSeqList(JavaMethod jmethod, List<JavaMethod> callingMethods, int count) {
        if (callingMethods.contains(jmethod) || MethodFinder.isTestMethod(jmethod)) {
            return;
        }

        callingMethods.add(jmethod);

        if (count == 0) {
            MethodSeq seq = new MethodSeq(callingMethods);
            methodSeqList.add(seq);
            return;
        }

        for (JavaMethod jm : jmethod.getCallingMethodsInProject()) {
            collectCallingMethodSeqList(jm, new ArrayList<JavaMethod>(callingMethods), count - 1);
        }
    }

    public static String getAnnotation(JavaMethod jmethod) {
        ASTNode node = jmethod.getASTNode();
        if (node instanceof MethodDeclaration) {
            MethodDeclaration methodDecl = (MethodDeclaration)node;
            for (Object obj : methodDecl.modifiers()) {
                IExtendedModifier mod = (IExtendedModifier)obj;
                if (mod.isAnnotation()) {
                    Annotation anno = (Annotation)mod;
                    Name name = anno.getTypeName();
                    return name.getFullyQualifiedName();

                }
            }
        }
        return "";
    }

    private static int getLoc(JavaMethod jmethod) {
        if (jmethod.getASTNode() instanceof MethodDeclaration) {
            MethodDeclaration methodDecl = (MethodDeclaration)jmethod.getASTNode();
            CompilationUnit cu = (CompilationUnit)methodDecl.getRoot();

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
}
