package org.example.testgen_cr.evosuite;

import org.example.testgen_cr.exception.TestGenException;
import org.example.testgen_cr.model.TestGenMethod;
import org.example.testgen_cr.model.TestGenMethodSeq;
import org.jtool.srcmodel.JavaProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EvosuiteRunner {
    private JavaProject targetProject;
    private TestGenMethodSeq targetMethodSeq;

    public EvosuiteRunner(TestGenMethodSeq targetMethodSeq) {
        this.targetProject = targetMethodSeq.getJavaProject();
        this.targetMethodSeq = targetMethodSeq;
    }

    public TestGenMethodSeq run(TestGenMethod evoTargetMethod) throws TestGenException {
        String classQualifiedName = evoTargetMethod.getClassQualifiedName();
        String targetJVMMethodPath = evoTargetMethod.getJVMMethodSignatureWithoutClassPath();
        String testOutDir = Paths.get(
                "evosuite",
                targetProject.getName(),
                "src"
                ,
                targetJVMMethodPath.replace(File.separatorChar, '_')
        ).toString();
        String command = buildEvoSuiteCommand(classQualifiedName, targetJVMMethodPath, testOutDir);

        System.out.println("Executing command: " + command);

        if (executeCommand(command)) {
            TestGenMethodSeq newMethodSeq = targetMethodSeq.createTargetMethodSeq(evoTargetMethod);
            newMethodSeq.setEvoTestPath(testOutDir);
            newMethodSeq.setEvoTargetMethod(evoTargetMethod);

            return newMethodSeq;
        }

        return null;
    }

    public String buildEvoSuiteCommand(String targetClass, String targetMethod, String testOutDir) {
        // Java 8対応のクラスパスを取得
        String fullClasspath = getFilteredClassPathExcludingIncompatibleVersions() + getProjectBinDirPath();

        String reportOutDir = testOutDir.replace(
                File.separator + "src" + File.separator,
                File.separator + "report" + File.separator
        );
        return String.format(
                "/Users/hara_ko/Library/Java/JavaVirtualMachines/corretto-1.8.0_412/Contents/Home/bin/java " +
                        "-jar libs/evosuite-1.2.0.jar " +
                        "-class %s " +
                        "-Dtarget_method_list=%s " +
                        "-Dassertion_strategy=ALL " +
                        "-Dnum_tests=500 " +
                        "-Dmax_size=200 " +
                        "-Dsearch_budget=20 " +
                        "-generateSuite " +
                        "-Duse_separate_classloader=false " +
                        "-Dnumber_of_tests_per_target=100 " +
                        "-projectCP %s " +
                        "-Dtest_dir=%s " +
                        "-Doutput_granularity=TESTCASE " +
                        "-Dreport_dir=%s " +
                        "-Dvirtual_net=false",
                targetClass, targetMethod, fullClasspath,
                testOutDir, reportOutDir
        );
    }

    private boolean executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            Process process = processBuilder.start();
            String prev_line = "null";

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!prev_line.equals(line)) {
                        System.out.println(line);
                        prev_line = line;
                    }
                }
                while ((line = errorReader.readLine()) != null) {
                    if (line.contains("UnsupportedClassVersionError")) {
                        throw new TestGenException("UnsupportedClassVersionError detected.");
                    }
                    if (line.contains("IllegalArgumentException") || line.contains("Cannot access statement")) {
                        throw new TestGenException("EvoSuiteコマンドエラー: " + line);
                    }
                    System.err.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new TestGenException("EvoSuite command failed with exit code: " + exitCode);
            }

            return true;
        } catch (TestGenException e) {
            System.err.println("Evosuite実行時エラー: " + e.getMessage());
            return false;
        } catch (Throwable e) {
            System.err.println("Evosuite原因不明エラー: " + e.getMessage());
            return false;
        }
    }

    private String getFilteredClassPathExcludingIncompatibleVersions() {
        String[] allClassPaths = targetProject.getClassPath();
        System.out.println(Arrays.toString(allClassPaths));

        List<String> compatibleClassPaths = Arrays.stream(allClassPaths)
                .filter(this::isJava8Compatible)
                .collect(Collectors.toList());

        if (compatibleClassPaths.isEmpty()) {
            return "";
        }

        return String.join(":", compatibleClassPaths) + ":";
    }

    private boolean isJava8Compatible(String classPathEntry) {
        if (classPathEntry.endsWith(".jar")) {
            return isJarJava8Compatible(classPathEntry);
        }
        // ディレクトリの場合の対応
        return true;
    }

    private boolean isJarJava8Compatible(String jarPath) {
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath)) {
            for (java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (java.io.InputStream inputStream = jarFile.getInputStream(entry)) {
                        if (!isClassFileJava8Compatible(inputStream)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading JAR file: " + jarPath + " -> " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean isClassFileJava8Compatible(java.io.InputStream classFileStream) throws java.io.IOException {
        byte[] header = new byte[8];
        if (classFileStream.read(header) < 8) {
            throw new java.io.IOException("Invalid class file");
        }
        if (header[0] != (byte) 0xCA || header[1] != (byte) 0xFE ||
                header[2] != (byte) 0xBA || header[3] != (byte) 0xBE) {
            throw new java.io.IOException("Invalid class file header");
        }
        int majorVersion = ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
        return majorVersion <= 52; // Java 8（52.0）以下で互換性あり
    }

    private String getProjectBinDirPath() {
        for (String binPath : targetProject.getBinaryPath()) {
            if (binPath.contains(File.separatorChar + "classes")) {
                System.out.println("getProjectBinDirPath: " + binPath);
                return binPath;
            }
        }
        return "";
    }
}
