package org.example.testgen_cr.method_searcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jtool.jxplatform.builder.ModelBuilder;
import org.jtool.jxplatform.builder.ModelBuilderBatch;
import org.jtool.srcmodel.JavaClass;
import org.jtool.srcmodel.JavaMethod;
import org.jtool.srcmodel.JavaProject;

public class SearchResultsChecker {

    private final static int DEFAULT_MIN_LOC = 1;
    private final static int CALLING_LIMIT = 1;
    private final static String[] primitiveTypes = {
            "java.lang.String",
            "java.lang.String[]",
            "String",
            "String[]",
            "byte",
            "byte[]",
            "short",
            "short[]",
            "int",
            "int[]",
            "long",
            "long[]",
            "float",
            "float[]",
            "boolean",
            "boolean[]",
            "char",
            "char[]"
    };

    private int notFoundClassNum = 0;
    private int notFoundMethodNum = 0;
    private int emptyInvariablesNum = 0;
    private int emptyOutVariablesNum = 0;
    private int oubjectInputNum = 0;
    private int objectOutputNum = 0;

    public void run(String name, String target) {
        run(name, target, DEFAULT_MIN_LOC);
    }

    private void run(String name, String target, int loc) {
        ModelBuilder builder = new ModelBuilderBatch();
        builder.analyzeBytecode(false);
        builder.useCache(true);
        builder.setConsoleVisible(true);

        List<JavaProject> targetProjects = builder.build(name, target);
        for (JavaProject jproject : targetProjects) {
            System.out.println("PROJECT: " + jproject.getName());

            MethodFinder methodFinder = new MethodFinder();
            methodFinder.run(jproject, loc, CALLING_LIMIT);
            List<JavaMethod> allMethods = methodFinder.getAllMethods();
            List<MethodSeq> targetMethodSeqList = methodFinder.getTargetMethodSeqList();

            List<CalleeMethod> targets = new ArrayList<>();
            FailedMethods failedMethods = new FailedMethods();

            for (MethodSeq seq : targetMethodSeqList) {
                CalleeMethod calleeMethod = new CalleeMethod(seq);
                boolean result = check(jproject, calleeMethod, failedMethods);
                if (result) {
                    targets.add(calleeMethod);
                }
            }

            System.out.println();
            System.out.println("# Found Method Call Sequences = " + allMethods.size());
            System.out.println("# Found Method Pair Of Caller And Callee = " + targetMethodSeqList.size());
            System.out.println("# Valid Method Pairs Of Caller And Callee = " + targets.size());
            System.out.println();

            Path outputFile = Paths.get(target, "../../methodPairFiles/" + jproject.getName() + "_methods.txt");

            try {
                // ファイルを作成（存在しない場合のみ）
                if (!Files.exists(outputFile)) {
                    Files.createFile(outputFile);
                }

                // File オブジェクトを Path オブジェクトから取得
                File file = outputFile.toFile();
                FileWriter filewriter = new FileWriter(file);

                List<String> targetMethods = new ArrayList<String>();
                for (CalleeMethod method : targets) {
                    if (!targetMethods.contains(method.getTargetMethod().getQualifiedName().fqn())) {
                        targetMethods.add(method.getTargetMethod().getQualifiedName().fqn());
                    }
                }

                filewriter.write("# Number Of Methods = " + allMethods.size() + "\n");
                filewriter.write("# Number Of Classes = " + methodFinder.getClassNum(jproject) + "\n");
                filewriter.write("# Line Of Code = " + methodFinder.getAllMethodLoc() + "\n");
                filewriter.write("# Target Methods = " + targetMethods.size() + "\n");
                filewriter.write("# All Method Pairs = " + targetMethodSeqList.size() + "\n");
                filewriter.write("# Valid Method Pairs = " + targets.size() + "\n");
                filewriter.write("# Not found target class = " + notFoundClassNum + "\n");
                filewriter.write("# Not found target method = " + notFoundMethodNum + "\n");
                filewriter.write("# Not found input variable = " + emptyInvariablesNum + "\n");
                filewriter.write("# Not found output variable = " + emptyOutVariablesNum + "\n");
                filewriter.write("# Input variable is not Object = " + oubjectInputNum + "\n");
                filewriter.write("# Output variable is not Primitive = " + objectOutputNum + "\n\n");

                // ファイルにメソッド情報を書き込む
                for (CalleeMethod method : targets) {
                    filewriter.write("[\ncallerMethod: " + method.getCallerMethod().getQualifiedName().fqn() + "\n");
                    filewriter.write("calleeMethod: " + method.getTargetMethod().getQualifiedName().fqn() + "\n]\n");
                }

                filewriter.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        builder.unbuild();
    }

    private boolean check(JavaProject jproject, CalleeMethod testTarget, FailedMethods failedMethods) {
        String targetClassName = testTarget.getTargetMethod().getDeclaringClass().getQualifiedName().fqn();
        JavaClass targetClass = jproject.getClass(targetClassName);

        boolean isTargetFlag = true;

        if (targetClass == null) {
            System.err.println("**** Not found target class: " + targetClassName);
            failedMethods.addNotFoundClasse(targetClassName);
            isTargetFlag = false;
            notFoundClassNum = notFoundClassNum + 1;
        }

        String targetMethodSig = testTarget.getTargetMethod().getSignature();
        JavaMethod targetMethod = targetClass.getMethod(targetMethodSig);

        if (targetMethod == null) {
            // System.err.println("**** Not found target method: " + targetMethodSig + " in " + targetClassName);
            failedMethods.addNotFoundMethod(targetMethodSig);
            isTargetFlag = false;
            notFoundMethodNum = notFoundMethodNum + 1;
        }

        if (testTarget.getInVariables().isEmpty()) {
            // System.err.println("**** Not found input variable: " + targetMethodSig + " in " + targetClassName);
            failedMethods.addNotFoundInputVariable(targetClassName);
            isTargetFlag = false;
            emptyInvariablesNum = emptyInvariablesNum + 1;
        }

        if (testTarget.getOutVariables().isEmpty()) {
            // System.err.println("**** Not found output variable: " + targetMethodSig + " in " + targetClassName);
            failedMethods.addNotFoundOutputVariable(targetClassName);
            isTargetFlag = false;
            emptyOutVariablesNum = emptyOutVariablesNum + 1;
        }

        if (!isObjectInput(testTarget)) {
            // System.err.println("**** Input variable is not Object: " + targetMethodSig + " in " + targetClassName);
            failedMethods.addIsNotObjectInput(targetMethodSig);
            isTargetFlag = false;
            oubjectInputNum = oubjectInputNum + 1;
        }

        if (!isPrimitiveOutput(testTarget)) {
            // System.err.println("**** Output variable is not Primitive: " + targetMethodSig + " in " + targetClassName);
            failedMethods.addIsNotPrimitiveOutput(targetMethodSig);
            isTargetFlag = false;
            objectOutputNum = objectOutputNum + 1;
        }

        return isTargetFlag;
    }

    private boolean isObjectInput(CalleeMethod testTarget) {
        boolean flag = true;

        for (ProjectVariable var : testTarget.getInVariables()) {
            String[] items = var.toString().split("\\s+");
            String[] argumentTypes = Arrays.copyOfRange(items, 1, items.length - 1);
            for (String argumentType : argumentTypes) {
                for (String primitiveType : primitiveTypes) {
                    if (argumentType.equals(primitiveType)) {
                        flag = false;
                    }
                }
                if (flag == true) {
                    return true;
                } else {
                    flag = true;
                }
            }
        }
        return false;
    }

    private boolean isPrimitiveOutput(CalleeMethod testTarget) {
        for (ProjectVariable var : testTarget.getOutVariables()) {
            String[] items = var.toString().split("@");
            String outPutType = items[1];
            for (String argumentType : primitiveTypes) {
                if (outPutType.equals(argumentType)) {
                    return true;
                }
            }
        }
        return false;
    }
}
