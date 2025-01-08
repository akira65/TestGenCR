package org.example.testgen_cr.analyzer;

import org.example.testgen_cr.exception.TestGenException;
import org.example.testgen_cr.model.TestGenMethodSeq;
import org.jtool.jxplatform.builder.ModelBuilderBatch;
import org.jtool.srcmodel.JavaProject;

import java.io.File;
import java.util.List;

public class MethodAnalyzer {
    private boolean logging;

    public MethodAnalyzer(boolean logging) {
        this.logging = logging;
    }

    public List<TestGenMethodSeq> analyze(String targetProjectPath, int callChainNum, int minLoc) throws TestGenException {
        String name = new File(targetProjectPath).getName();
        ModelBuilderBatch builder = new ModelBuilderBatch();
        builder.setConsoleVisible(this.logging);

        List<JavaProject> targetProjects = builder.build(name, targetProjectPath);
        if (targetProjects.isEmpty()) {
            throw new TestGenException("No projet found at: " + targetProjectPath);
        }
        JavaProject jproject = targetProjects.get(0);

        MethodFinder methodFinder = new MethodFinder(jproject.getModelBuilder());

        List<TestGenMethodSeq> methodSeqList = methodFinder.collectAllMethodSeq(jproject, callChainNum, minLoc);


//        for (TestGenMethodSeq seq : methodSeqList) {
//            for (TestGenMethod method : seq.getMethodChain()) {
//                if (method.getCallNode() != null) {
//                    System.out.println("----");
//                    if (seq.getCallerMethod(method) != null) {
//                        System.out.println("Caller: " + seq.getCallerMethod(method).getQualifiedName());
//                    }
//                    System.out.println("Target: " + method.getQualifiedName());
//                    System.out.println("Callee: " + method.getCallNode().getQualifiedName().fqn());
//                    System.out.println("Full Return Type: " + method.getFullReturnType());
//                    System.out.println("VariableType: " + method.getFullVariableTypeList());
//                    System.out.println("Arguments: " + method.getCallNode().getArgumentSize());
//                    System.out.println("----");
//                } else {
//                    System.out.println("sssssssssss");
//                    System.out.println(seq.getSeqMethod(0).getJavaMethod().getQualifiedName());
//                    System.out.println("Caller: " + method.getJavaMethod().getQualifiedName());
//                }
//            }
//            System.out.println("---------------------------------------");
//            System.out.println(seq.toString());
//        }
        return methodSeqList;
    }
}

