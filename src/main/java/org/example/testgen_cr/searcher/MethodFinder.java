package org.example.testgen_cr.searcher;

import org.eclipse.jdt.core.dom.*;
import org.example.testgen_cr.model.TestGenMethod;
import org.example.testgen_cr.model.TestGenMethodSeq;
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
    private final ModelBuilder builder;
    private static final boolean nonPublicFlag = true;
    private int minLoc;

    public MethodFinder(ModelBuilder builder) {
        this.builder = builder;
    }

    public List<TestGenMethodSeq> collectAllMethodSeq(JavaProject jproject, int callChainNum, int minLoc) {
        List<TestGenMethodSeq> allSeq = new ArrayList<>();

        for (JavaClass jc : jproject.getClasses()) {
            for (JavaMethod jm : jc.getMethods()) {
                if (isTarget(jm) && getLoc(jm) >= minLoc) {
                    List<TestGenMethodSeq> seqs = collectCallingMethodSeqList(jm, callChainNum);
                    allSeq.addAll(seqs);
                }
            }
        }
        return allSeq;
    }

    public List<TestGenMethodSeq> collectCallingMethodSeqList(JavaMethod jmethod, int callChainNum) {
        List<TestGenMethodSeq> methodSeqList = new ArrayList<>();
        Set<String> uniqueSeqs = new HashSet<>();

        collect(jmethod, methodSeqList, new ArrayList<>(), callChainNum, uniqueSeqs);

        return methodSeqList;
    }

    private void collect(JavaMethod jmethod, List<TestGenMethodSeq> methodSeqList, List<JavaMethod> callingMethodList, int callChainNum, Set<String> uniqueSeqs) {
        if (callChainNum < 0 || callingMethodList.contains((jmethod))) {
            return;
        }

        if (!isTarget(jmethod)) {
            return;
        }

        callingMethodList.add(jmethod);

        if (callChainNum == 0) {
            List<TestGenMethod> methodChain = buildMethodChain(callingMethodList);
            String seqIdentifier = generateSequenceIdentifier(methodChain);

            if (!uniqueSeqs.contains(seqIdentifier)) {
                TestGenMethodSeq seq = new TestGenMethodSeq(methodChain);
                methodSeqList.add(seq);
                uniqueSeqs.add(seqIdentifier);
            }
            return;
        }

        for (JavaMethod caller : jmethod.getCallingMethodsInProject()) {
            collect(caller, methodSeqList, new ArrayList<>(callingMethodList), callChainNum - 1, uniqueSeqs);
        }
    }

    private List<TestGenMethod> buildMethodChain(List<JavaMethod> methodList) {
        List<TestGenMethod> methodChain = new ArrayList<>();

        for (int index = 0; index < methodList.size(); index++) {
            JavaMethod jmethod = methodList.get(index);
            TestGenMethod tmethod;
            CFG cfg = builder.getCFG(jmethod);

            if (index == 0) {
                tmethod = new TestGenMethod(cfg, jmethod, null);
                methodChain.add(tmethod);
            } else {
                JavaMethod callingMethod = methodList.get(index - 1);
                CFGMethodCall callNode = findCallNode(callingMethod, jmethod);
                tmethod = new TestGenMethod(cfg, jmethod, callNode);
                methodChain.add(tmethod);
            }
        }

        return methodChain;
    }

    private String generateSequenceIdentifier(List<TestGenMethod> methodChain) {
        StringBuilder sb = new StringBuilder();
        for (TestGenMethod method : methodChain) {
            sb.append(method.getJavaMethod().getQualifiedName().fqn()).append("<-");
        }
        return sb.toString();
    }

    private boolean isTarget(JavaMethod jmethod) {
        return !isTestMethod(jmethod)
                && !jmethod.isConstructor()
                && jmethod.getDeclaringClass().getTypeBinding().isTopLevel()
                && jmethod.isInProject()
                && (jmethod.isPublic() || nonPublicFlag);
    }

    private static boolean isTestMethod(JavaMethod jmethod) {
        JavaClass jclass = jmethod.getDeclaringClass().getSuperClass();
        if (jclass != null) {
            if (jclass.getQualifiedName().fqn().equals("junit.framework.TestCase")) {
                return true;
            }

            String sourcePath = jmethod.getFile() != null ? jmethod.getFile().getPath() : "";
            return !isInMainDirectory(sourcePath);
        }
        return false;
    }

    public static boolean isInMainDirectory(String sourcePath) {
        if (sourcePath.isEmpty()) {
            return true;
        }

        String normalizedPath = sourcePath.replace("\\", "/");
        return normalizedPath.contains("/src/main/");
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

    private CFGMethodCall findCallNode(JavaMethod callee, JavaMethod caller) {
        CFG callerCFG = builder.getCFG(caller);
        for (CFGNode node : callerCFG.getNodes()) {
            if (node.isMethodCall()) {
                CFGMethodCall callNode = (CFGMethodCall) node;
                if (callNode.getQualifiedName().equals(callee.getQualifiedName())) {
                    return callNode;
                }
            }
        }
        return null;
    }

    public static int getLoc(JavaMethod jmethod) {
        if (jmethod.getASTNode() instanceof MethodDeclaration methodDecl) {
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

            return (bottomLineNumber - upperLineNumber) - (docbottomLineNumber - docupperLineNumber);
        }
        return 0;
    }
}
