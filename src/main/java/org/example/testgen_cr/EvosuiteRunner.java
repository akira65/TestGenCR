//package org.example.testgen_cr;
//
//import org.example.testgen_cr.model_buffs.TestTarget;
//import org.jtool.srcmodel.JavaLocalVar;
//import org.jtool.srcmodel.JavaMethod;
//import org.jtool.srcmodel.JavaProject;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.List;
//
//public class EvosuiteRunner {
//    private JavaProject targetProject;
//    private JavaMethod targetMethod;
//
//    public EvosuiteRunner(TestTarget targetMethod) {
//        this.targetProject = targetMethod.getTargetJavaProject();
//        this.targetMethod = targetMethod.getTargetJavaMethod();
//    }
//
//    public void runEvosuite() throws TestGenException {
//        System.out.println(targetProject.getBinaryPath()[1]);
//        System.out.println("jjjjjjjjjjjjjjjjjj" + targetMethod.getQualifiedName() + "sdkjfjalk");
//        System.out.println("jjjjjjjjjjjjjjjjjj" + targetMethod.getClassName() + "sdkjfjalk");
//        System.out.println("jjjjjjjjjjjjjjjjjj" + targetMethod.getDeclaringClass().getClassName() + "sdkjfjalk");
//
/// /        to
/// / /Users/hara_ko/Library/Java/JavaVirtualMachines/corretto-1.8.0_412/Contents/Home/bin/java -jar libs/evosuite-1.2.0.jar -class spark.utils.IOUtils -projectCP /Users/hara_ko/Desktop/TestGenCR/testGenTarget/spark-master/target/classes -Dsearch_budget=20 -Dnumber_of_tests_per_target=60 -generateSuite -Dtest_dir=evosuite/out -Dreport_dir=evosuite/out -Duse_separate_classloader=false
/// //Users/hara_ko/Library/Java/JavaVirtualMachines/corretto-1.8.0_412/Contents/Home/bin/java -jar libs/evosuite-1.2.0.jar -class spark.utils.IOUtils -Dtarget_method=spark.utils.IOUtils#toByteArray -projectCP /Users/hara_ko/Desktop/TestGenCR/testGenTarget/spark-master/target/classes -Dsearch_budget=200 -Dnumber_of_tests_per_target=60 -generateSuite -Dtest_dir=evosuite/out -Dreport_dir=evosuite/out -Duse_separate_classloader=false
/// /
/// /
/// /                /Users/hara_ko/Library/Java/JavaVirtualMachines/corretto-1.8.0_412/Contents/Home/bin/java \
/// /        -jar libs/evosuite-1.2.0.jar \
/// /        -class spark.utils.IOUtils \
/// /        "-Dtarget_method_list=toByteArray(Ljava/io/InputStream;)[B" \
/// /        -Dnum_tests=100 \
/// /        -Dmax_size=200 \
/// /        -Dsearch_budget=120 \
/// /        -Dstop_zero=false \
/// /        -Duse_separate_classloader=false \
/// /        -Dcriterion=LINE:BRANCH:EXCEPTION:OUTPUT:METHOD:METHODNOEXCEPTION:CBRANCH \
/// /        -projectCP "/Users/hara_ko/Desktop/TestGenCR/testGenTarget/spark-master/target/classes" \
/// /        -Dtest_dir=evosuite/out \
/// /        -Dreport_dir=evosuite/out
/// /
/// /        String command = "/Users/hara_ko/Library/Java/JavaVirtualMachines/corretto-1.8.0_412/Contents/Home/bin/java -jar libs/evosuite-1.2.0.jar -class " + targetMethod.getClassName() + " -Dtarget_method " + targetMethod.getClassName() + "." + targetMethod.getName() + " -projectCP " + targetProject.getBinaryPath()[1] + " -Dsearch_budget=200 -Dnumber_of_tests_per_target=60 -generateSuite -Dtest_dir=evosuite/out -Dreport_dir=evosuite/out -Duse_separate_classloader=false";
/// /        System.out.println(command);
/// /
/// /        executeCommand(command);
//    }
//
//
//    public static void executeCommand(String command) {
//        try {
//            // コマンドを分割してProcessBuilderに渡す
//            String[] commandArray = command.split(" ");
//            ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
//
//            // プロセスを開始
//            Process process = processBuilder.start();
//
//            // 標準出力を読み取る
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//            }
//
//            // 標準エラーを読み取る（エラー内容を確認するため）
//            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
//                String line;
//                while ((line = errorReader.readLine()) != null) {
//                    System.err.println(line);
//                }
//            }
//
//            // プロセスの終了を待機
//            int exitCode = process.waitFor();
//            System.out.println("Process exited with code: " + exitCode);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String toJVMDescriptor(JavaMethod method) {
//        List<JavaLocalVar> params = method.getParameters();
//        List<String> paramTypes = params.stream().map(JavaLocalVar::getType).toList();
//        String returnType = method.getReturnType();
//
//        StringBuilder descriptor = new StringBuilder();
//        descriptor.append("(");
//        System.out.println(toJVMtypeDescriptor(method));
////        for (String paramType : praramTypes) {
////            descriptor.append(toJVMDescriptor(method));
////        })
//        return "aa";
//    }
//
//    private static String toJVMTypeDescriptor(String javaType) {
//        // プリミティブ型の場合のマッピング
//        switch (javaType) {
//            case "int":
//                return "I";
//            case "boolean":
//                return "Z";
//            case "byte":
//                return "B";
//            case "char":
//                return "C";
//            case "short":
//                return "S";
//            case "long":
//                return "J";
//            case "float":
//                return "F";
//            case "double":
//                return "D";
//            case "void":
//                return "V";
//        }
//
//        // 配列型の場合
//        // 例: java.lang.String[] -> Ljava.lang.String; の前に '[' を付与
//        if (javaType.endsWith("[]")) {
//            // 次元数をカウント
//            int dim = 0;
//            while (javaType.endsWith("[]")) {
//                javaType = javaType.substring(0, javaType.length() - 2);
//                dim++;
//            }
//            // javaTypeは配列要素型、これをJVM記述子に変換
//            String elementDescriptor = toJVMTypeDescriptor(javaType);
//            StringBuilder arrayDescriptor = new StringBuilder();
//            for (int i = 0; i < dim; i++) {
//                arrayDescriptor.append("[");
//            }
//            arrayDescriptor.append(elementDescriptor);
//            return arrayDescriptor.toString();
//        }
//
//        // オブジェクト型の場合
//        // 完全修飾名を "/" 区切りに変換
//        String objectType = "L" + javaType.replace('.', '/') + ";";
//        return objectType;
//    }
//
//}
