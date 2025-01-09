package org.example.testgen_cr.evosuite;

import org.example.testgen_cr.exception.TestGenException;
import org.example.testgen_cr.model.TestGenMethodSeq;
import org.example.testgen_cr.model.TestGenParts;
import org.jtool.srcmodel.JavaProject;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvosuiteTestRunner {
    List<String> jarFileList = new ArrayList<>(List.of(
            "libs/evosuite-standalone-runtime-1.2.0.jar",
            "libs/hamcrest-2.2.jar",
            "libs/junit-4.13.2.jar",
            "libs/mockito-core-5.9.0.jar"
    ));

    /**
     * コンパイルメソッド
     *
     * @param seq テスト生成シーケンス情報
     */
    public void compile(TestGenMethodSeq seq) {
        List<String> classPathList = new ArrayList<>();
        classPathList.addAll(jarFileList);
        classPathList.addAll(Arrays.asList(seq.getJavaProject().getClassPath()));
        classPathList.add(getProjectBinDirPath(seq.getJavaProject()));

        String classPathOption = " -classpath " + getPathString(classPathList);
        String sourcePathOption = " -sourcepath " + seq.getEvoTestPath();
        String binaryPathOption = " -d " + seq.getEvoTestBinPath();

        System.out.println("classPathOption: " + classPathOption);
        System.out.println("binaryPathOption: " + binaryPathOption);
        System.out.println("sourcePathOption: " + sourcePathOption);
        List<String> testFiles = new ArrayList<>();
        collectTestFiles(new File(seq.getEvoTestPath()), testFiles);

        if (testFiles.isEmpty()) {
            System.out.println("Evosuiteのテストが見つかりません");
            return;
        }
        String optionsStr = classPathOption + sourcePathOption + binaryPathOption;

        String[] options = optionsStr.trim().split(" ");

        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ByteArrayOutputStream os2 = new ByteArrayOutputStream();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        String[] compilerArgs = new String[options.length + testFiles.size()];
        System.arraycopy(options, 0, compilerArgs, 0, options.length);
        for (int i = 0; i < testFiles.size(); i++) {
            compilerArgs[options.length + i] = testFiles.get(i);
        }

        int compilationResult = compiler.run(null, os1, os2, compilerArgs);

        if (compilationResult != 0) {
            System.out.println("コンパイルエラー: " + os2.toString());
            throw new RuntimeException("テストのコンパイルに失敗しました。");
        }

        System.out.println("テストのコンパイルに成功しました。");

        // デバッグ: コンパイルされたクラスファイルをリスト表示
        File binaryDir = new File(seq.getEvoTestBinPath());
        if (binaryDir.exists() && binaryDir.isDirectory()) {
            System.out.println("コンパイルされたクラスファイル:");
            listFilesRecursively(binaryDir, "");
        } else {
            System.out.println("コンパイルされたクラスディレクトリが存在しません: " + binaryDir.getPath());
        }
    }

    /**
     * テストクラスを実行するメソッド
     *
     * @param seq         テスト生成シーケンス情報
     * @param javaProject ターゲットJavaプロジェクト
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public List<TestGenParts> runEvoTest(TestGenMethodSeq seq, JavaProject javaProject) throws IOException, ClassNotFoundException, TestGenException {
        String evoTestBinPath = seq.getEvoTestBinPath();
        System.out.println("evoTestBinPath: " + evoTestBinPath);
        URLClassLoader targetClassLoader = createClassLoader(javaProject, evoTestBinPath);

        String targetClassName = seq.getEvoTargetMethod().getClassName();
        // テストクラスの収集（テストクラスのみ）
        List<Class<?>> testClasses = collectTestClasses(evoTestBinPath, targetClassLoader, targetClassName);
        System.out.println("Test classes found: " + testClasses);

        // テストの実行
        List<TestGenParts> testGenPartsList = new ArrayList<TestGenParts>();
        runTests(testClasses, seq, testGenPartsList);

        return testGenPartsList;
    }

    /**
     * テストクラスを検索するメソッド
     *
     * @param testsDir    テストクラスが存在するディレクトリ
     * @param classLoader クラスローダー
     * @return テストクラスのリスト
     * @throws ClassNotFoundException
     */
    private List<Class<?>> collectTestClasses(String testsDir, URLClassLoader classLoader, String targetClassName) throws ClassNotFoundException {
        List<Class<?>> testClasses = new ArrayList<>();
        File testClassesDirectory = new File(testsDir);
        if (!testClassesDirectory.exists() || !testClassesDirectory.isDirectory()) {
            System.out.println("テストクラスディレクトリが存在しないか、ディレクトリではありません: " + testClassesDirectory.getPath());
            return testClasses;
        }

        // 再帰的にクラスファイルを探す
        List<String> classNames = new ArrayList<>();
        findClasses(testClassesDirectory, testClassesDirectory, classNames);

        System.out.println("classNames: " + classNames);
        // テストクラスをロード（_ESTest を含むクラスのみ）
        for (String className : classNames) {
            if (!isTestClass(className, targetClassName)) {
                continue; // テストクラスのみを収集
            }
            try {
                Class<?> cls = Class.forName(className, true, classLoader);
                testClasses.add(cls);
                System.out.println("Loaded test class: " + cls.getName());
            } catch (ClassNotFoundException e) {
                System.out.println("Failed to load test class: " + className);
                // クラスが見つからない場合はスキップ
            }
        }

        return testClasses;
    }

    private void findClasses(File rootDir, File currentDir, List<String> classNames) {
        for (File file : currentDir.listFiles()) {
            if (file.isDirectory()) {
                findClasses(rootDir, file, classNames);
            } else if (file.getName().endsWith(".class")) {
                String relativePath = rootDir.toURI().relativize(file.toURI()).getPath();
                String className = relativePath.replace("/", ".").replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * クラスがテストクラスかどうかを判定するメソッド
     *
     * @param testClassName クラス名
     * @return テストクラスであれば true、そうでなければ false
     */
    private static boolean isTestClass(String testClassName, String targetClassName) {
        return testClassName.contains(targetClassName + "_ESTest") && !testClassName.contains("_ESTest_scaffolding");
    }

    /**
     * テストクラスを実行するメソッド
     *
     * @param testClasses      テストクラスのリスト
     * @param testGenPartsList ターゲットクラスローダー
     */
    private void runTests(List<Class<?>> testClasses, TestGenMethodSeq seq, List<TestGenParts> testGenPartsList) throws TestGenException {
        if (testClasses.isEmpty()) {
            System.out.println("実行するテストクラスが見つかりません。");
            return;
        }

        // 元の標準出力を保存
        PrintStream originalOut = System.out;

        // キャプチャ用のストリーム
        ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
        PrintStream capturingPrintStream = new PrintStream(capturedOut);

        // 標準出力をリダイレクト
        System.setOut(capturingPrintStream);

        try {
            JUnitCore junit = new JUnitCore();
            for (Class<?> testClass : testClasses) {
                System.out.println("Running tests in class: " + testClass.getName());
                try {
                    Result result = junit.run(testClass);

                    for (Failure failure : result.getFailures()) {
                        System.out.println("Test failed: " + failure.toString());
                        failure.getException().printStackTrace(System.out);
                    }

                    System.out.println("Tests run: " + result.getRunCount() +
                            ", Failures: " + result.getFailureCount() +
                            ", Ignored: " + result.getIgnoreCount());
                    System.out.println("Was successful: " + result.wasSuccessful());
                    System.out.println("--------------------------------------------------");
                } catch (Exception e) {
                    System.out.println("Failed to run tests in class: " + testClass.getName());
                    e.printStackTrace(System.out);
                }
            }
        } finally {
            // 標準出力を元に戻す
            System.setOut(originalOut);
        }

        // キャプチャした内容を取得
        String outputLog = capturedOut.toString();

        // キャプチャした内容を出力（必要なら保存も可能）
        System.out.println("===========Captured Output============");

        List<String> logInfoList = new ArrayList<String>();
        for (String line : outputLog.split("\n")) {
            if (line.contains("TESTGEN_LOGCODE")) {
                System.out.println(line.split("TESTGEN_LOGCODE")[1].replaceAll("\\s", ""));
                logInfoList.add(line.split("TESTGEN_LOGCODE")[1].replaceAll("\\s", ""));
            }
            if (line.contains("successful")) {
                System.out.println(line);
            }
        }

        for (int i = 0; i < logInfoList.size(); i++) {
            if (logInfoList.get(i).equals("LogInfoStart")) {
                System.out.println("LogGET");
                TestGenParts testGenParts = new TestGenParts(seq);
                for (int n = i + 1; n < logInfoList.size(); n++) {
                    String logInfo = logInfoList.get(n);
                    if (logInfo.equals("LogInfoStart") || logInfo.isEmpty() || n == logInfoList.size() - 1) {
                        if (!testGenParts.getReturnInfoList().isEmpty()) {
                            testGenPartsList.add(testGenParts);
                        }
                        break;
                    }
                    if (logInfo.equals("args:{")) {
                        for (int j = n + 1; j < logInfoList.size(); j++) {
                            logInfo = logInfoList.get(j);
                            if (logInfoList.get(j).equals("}")) {
                                break;
                            }
                            String[] variableInfo = logInfo.split(":");
                            testGenParts.setVariableInfo(variableInfo[0], variableInfo[1], variableInfo[2]);
                        }
                    }
                    if (logInfo.equals("fields:{")) {
                        for (int j = n + 1; j < logInfoList.size(); j++) {
                            logInfo = logInfoList.get(j);
                            if (logInfoList.get(j).equals("}")) {
                                break;
                            }
                            String[] variableInfo = logInfo.split(":");
                            testGenParts.setVariableInfo(variableInfo[0], variableInfo[1], variableInfo[2]);
                        }
                    }
                    if (logInfo.equals("return:{")) {
                        String[] returnInfo = logInfoList.get(n + 1).split(":");
                        testGenParts.setReturnInfo(returnInfo[0], returnInfo[1], returnInfo[2]);
                    }
                }
            }
        }
    }

    /**
     * クラスローダーを作成するメソッド
     *
     * @param targetProject  ターゲットプロジェクト
     * @param evoTestBinPath Evosuiteのバイナリ出力パス
     * @return クラスローダー
     * @throws IOException
     */
    private URLClassLoader createClassLoader(JavaProject targetProject, String evoTestBinPath) throws IOException {

        List<URL> urlList = new ArrayList<>();

        for (String jar : jarFileList) {
            urlList.add(new File(jar).toURI().toURL());
        }

        for (String jar : targetProject.getClassPath()) {
            urlList.add(new File(jar).toURI().toURL());
        }
        // Evosuiteのバイナリ出力パスの追加
        urlList.add(new File(evoTestBinPath).toURI().toURL());

        // TARGET_CLASSES_DIRディレクトリの追加
        urlList.add(new File(getProjectBinDirPath(targetProject)).toURI().toURL());

        URL[] urls = urlList.toArray(new URL[0]);
        System.out.println("URLS: " + Arrays.toString(urls));

        URLClassLoader urlClassLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

        return urlClassLoader;
    }

    /**
     * テストファイルを収集するメソッド
     *
     * @param dir      テストソースコードのディレクトリ
     * @param fileList ファイルリスト
     */
    private static void collectTestFiles(File dir, List<String> fileList) {
        if (!dir.exists()) {
            System.out.println("Evosuiteテストディレクトリが存在しません: " + dir.getPath());
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                collectTestFiles(file, fileList);
            } else if (file.getName().endsWith(".java")) {
                fileList.add(file.getPath());
            }
        }
    }

    /**
     * クラスパスの文字列表現を取得するメソッド
     *
     * @param paths パスのリスト
     * @return クラスパスの文字列
     */
    private static String getPathString(List<String> paths) {
        StringBuilder sb = new StringBuilder();
        if (!paths.isEmpty()) {
            sb.append(paths.get(0));
            for (int i = 1; i < paths.size(); i++) {
                sb.append(File.pathSeparatorChar);
                sb.append(paths.get(i));
            }
        }
        return sb.toString();
    }

    /**
     * プロジェクトのバイナリディレクトリパスを取得するメソッド
     *
     * @param targetProject ターゲットプロジェクト
     * @return バイナリディレクトリパス
     */
    private static String getProjectBinDirPath(JavaProject targetProject) {
        for (String binPath : targetProject.getBinaryPath()) {
            if (binPath.contains(File.separator + "classes")) {
                return binPath;
            }
        }
        return "";
    }

    /**
     * クラスファイルを再帰的にリスト表示するメソッド
     *
     * @param dir    ディレクトリ
     * @param indent インデント文字列
     */
    private static void listFilesRecursively(File dir, String indent) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                System.out.println(indent + "[DIR] " + file.getName());
                listFilesRecursively(file, indent + "  ");
            } else {
                System.out.println(indent + file.getName());
            }
        }
    }
}
