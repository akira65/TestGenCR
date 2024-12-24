//package org.example.testgen_cr;
//
//import org.example.testgen_cr.model_buffs.TestGenMethodSeq;
//import org.example.testgen_cr.model_buffs.TestGenVariable;
//import org.example.testgen_cr.model_buffs.TestTarget;
//import org.example.testgen_cr.results.GenClass;
//import org.jtool.jxplatform.builder.ModelBuilderBatch;
//import org.jtool.srcmodel.JavaClass;
//import org.jtool.srcmodel.JavaFile;
//import org.jtool.srcmodel.JavaMethod;
//import org.jtool.srcmodel.JavaProject;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//
//public class TestGenerator {
//
//    public static final String TESTGEN_NAME_POSTFIX = "_AutoGen";
//    public static final String TESTGEN_HEADER_COMMENT = "THIS CODE WAS AUTOMATICALLY GENERATED FROM TestGenCR";
//    public static final String LINE_SEP = System.getProperty("line.separator");
//
//    private static final String LOGFILE_PREFIX = "####TESTGEN####";
//    private static final String LOGFILE_EXT = ".log";
//
//    private boolean EXECUTION_DATA_REMOVED = false;
//    private File executionDataFile;
//
//    private ModelBuilderBatch builder;
//    private boolean binaryAnalysis = true;
//    private boolean useCache = true;
//
//    private boolean verbose = true;
//
//    private List<JavaProject> targetProjects;
//    private TestTarget testTarget;
//    private Set<JavaFile> compileFiles;
//    private Set<JavaMethod> testMethods;
//
//    private final boolean statusReport = true;
//
//
//    public TestGenerator(String name, String target, boolean logging) {
//        TestGenerator.removeAutoGenJavaFile(target);
//
//        builder = new ModelBuilderBatch(binaryAnalysis, useCache);
//        builder.setConsoleVisible(logging);
//
//        targetProjects = builder.build(name, target);
//    }
//
//    public List<GenClass> generate(String className, String methodSig) throws TestGenException {
//        JavaProject targetProject = targetProjects.getFirst();
//        if (targetProject == null) {
//            System.err.println("Not found target project");
//            return new ArrayList<>();
//        }
//
//        JavaClass targetClass = targetProject.getClass(className);
//        if (targetClass == null) {
//            System.err.println("Not found target class: " + className);
//            return new ArrayList<>();
//        }
//
//        // メソッド一覧を出力して確認
//        System.out.println("Methods in class " + className + ":");
//        for (JavaMethod method : targetClass.getSortedMethods()) {
//            System.out.println(" - " + method.getSignature());
//        }
//
//        JavaMethod targetMethod = targetClass.getMethod(methodSig);
//        if (targetMethod == null || !targetMethod.isMethod()) {
//            System.err.println("Not found target method: " + methodSig + " in " + className);
//            return new ArrayList<>();
//        }
//
//        return generate(targetMethod);
//    }
//
//
//    public List<GenClass> generate(JavaMethod targetMethod) throws TestGenException {
//        JavaProject targetProject = targetMethod.getJavaProject();
//        try {
//            testTarget = collectTestInfo(targetMethod);
//            prepareLogfile(targetProject);
//            preCheck(targetProject);
//            EvosuiteRunner evosuiteRunner = new EvosuiteRunner(testTarget);
//            evosuiteRunner.runEvosuite();
//
//            if (verbose) {
//                System.out.println("-target method: " + targetMethod.getQualifiedName());
//
//                if (statusReport) {
//                    testTarget.sortTestGenVariableList();
//                    testTarget.sortTestGenMethodSeqList();
//                    for (TestGenVariable var : testTarget.getInVariables()) {
//                        System.out.println("IN: " + var.toString());
//                    }
//                    for (TestGenVariable var : testTarget.getOutVariables()) {
//                        System.out.println("OUT: " + var.toString());
//                    }
//                    for (TestGenMethodSeq seq : testTarget.getMethodSeqList()) {
//                        System.out.println("SEQ: " + seq.toString2());
//                    }
//                }
//            }
//            return new ArrayList<>();
//        } catch (TestGenException e) {
//            try {
//                if (testTarget != null) {
//                    compile(testTarget.getRelatedFiles(), targetProject);
//                }
//            } catch (TestGenException e2) { /* empty */ }
//
//            System.err.println(e.getMessage());
//            if (executionDataFile != null) {
//                if (!verbose) {
//                    executionDataFile.delete();
//                }
//            }
//            throw e;
//        }
//    }
//
//    private TestTarget collectTestInfo(JavaMethod targetMethod) throws TestGenException {
//        TestTarget testTarget = new TestTarget(targetMethod);
//        testTarget.collectTestInfo();
//
/// /        返り値がないとき
//        if (testTarget.getOutVariables().isEmpty()) {
//            throw new TestGenException("Cannot generate test methods because there is no observable out variable.");
//        }
////        テストメソッド・呼び出しメソッドが存在しない
//        if (testTarget.getMethodSeqList().isEmpty()) {
//            throw new TestGenException("Cannot generate test methods because no test method was found.");
//        }
//
//        if (verbose) {
//            System.out.println("-Identified variables to be monitored and found test methods");
//        }
//        System.out.println("--------------------------------" + testTarget.getMethodSeqList().get(0).toString2());
//        return testTarget;
//    }
//
//    // TestGenCRによって生成されたファイルを削除
//    private static void removeAutoGenJavaFile(String path) {
//        try {
//            for (File file : collectAllJavaFiles(path)) {
//                if (file.toPath().toString().contains(TESTGEN_NAME_POSTFIX)) {
//                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//                        reader.readLine();
//                        String line = reader.readLine();
//                        if (line != null && line.contains(TESTGEN_HEADER_COMMENT)) {
//                            if (!file.delete()) {
//                                System.err.println("Failed to delete file: " + file.getPath());
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//        }
//    }
//
//
//    private static List<File> collectAllJavaFiles(String path) {
//        List<File> files = new ArrayList<File>();
//        File res = new File(path);
//        if (res.isFile()) {
//            if (path.endsWith(".java")) {
//                files.add(res);
//            }
//        } else if (res.isDirectory()) {
//            for (File r : Objects.requireNonNull(res.listFiles())) {
//                files.addAll(collectAllJavaFiles((r.getPath())));
//            }
//        }
//
//        return files;
//    }
//
//    private void prepareLogfile(JavaProject targetProject) {
//        String binpath = targetProject.getBinaryPath()[0];
//        String logpath = binpath + File.separatorChar + LOGFILE_PREFIX + testTarget.getTargetJavaMethod().getName() + LOGFILE_EXT;
//        executionDataFile = new File(logpath);
//
//        if (executionDataFile.exists()) {
//            if (!executionDataFile.delete()) {
//                System.err.println("Cannot delete log file: " + logpath);
//            }
//        }
//    }
//
//    private void preCheck(JavaProject targetProject) throws TestGenException {
//        compileFiles = compile(testTarget.getRelatedFiles(), targetProject);
//        testMethods = runTest(testTarget.getTestJavaMethods(), targetProject);
//
//        if (verbose) {
//            System.out.println("-Checked compilation and test running prior to the generation.");
//        }
//    }
//
//    private Set<JavaFile> compile(Set<JavaFile> files, JavaProject targetProject) throws TestGenException {
//        return TestGenTestRunner.compile(files, targetProject);
//    }
//
//    private Set<JavaMethod> runTest(Set<JavaMethod> methods, JavaProject targetProject) throws TestGenException {
//        return TestGenTestRunner.runTest(methods, targetProject);
//    }
//}
