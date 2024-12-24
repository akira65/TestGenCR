package org.example.testgen_cr;

import org.example.testgen_cr.results.TestGenClassLoader;
import org.jtool.srcmodel.JavaClass;
import org.jtool.srcmodel.JavaFile;
import org.jtool.srcmodel.JavaMethod;
import org.jtool.srcmodel.JavaProject;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class TestGenTestRunner {
    public static final int JDK_VERSION = jdkVersion();

    private static int jdkVersion() {
        return Integer.parseInt(System.getProperty("java.version").split("\\.")[0]) > 8 ? 11 : 8;
    }

    public static Set<JavaFile> compile(Set<JavaFile> files, JavaProject jproject) throws TestGenException {
        Set<JavaFile> successFiles = new HashSet<>();
        String classPathOption = "";
        String sourcePathOption = "";
        String binaryPathOption = "";

        if (jproject.getClassPath().length > 0) {
            classPathOption = " -classpath " + getPathString(jproject.getClassPath());
        }
        if (jproject.getSourcePath().length > 0) {
            sourcePathOption = " -sourcepath " + getPathString(jproject.getSourcePath());
        }
        if (jproject.getBinaryPath().length > 0) {
            binaryPathOption = " -d " + jproject.getBinaryPath()[0];
        }
        String targetOption = " -source " + JDK_VERSION + " -target " + JDK_VERSION;
        String optionsStr = classPathOption + sourcePathOption + binaryPathOption + targetOption;

        String[] options = optionsStr.trim().split(" ");
        String[] args = new String[options.length + 1];
        System.arraycopy(options, 0, args, 0, options.length);

        for (JavaFile jfile : files) {
//            標準出力用
            ByteArrayOutputStream os1 = new ByteArrayOutputStream();
//            標準エラー出力用
            ByteArrayOutputStream os2 = new ByteArrayOutputStream();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            args[options.length] = jfile.getPath();
            int compileOk = compiler.run(null, os1, os2, args);
            if (compileOk == 0) {
                successFiles.add(jfile);
            } else {
                throw new TestGenException("Cannot compile files related to test methods.");
            }
        }
        return successFiles;
    }

    private static String getPathString(String[] paths) {
        StringBuilder sb = new StringBuilder();
        if (paths.length > 0) {
            sb.append(paths[0]);
            for (int i = 1; i < paths.length; i++) {
                sb.append(File.pathSeparatorChar);
                sb.append(paths[i]);
            }
        }
        return sb.toString();
    }

    public static Set<JavaMethod> runTest(Set<JavaMethod> methods, JavaProject jproject) throws TestGenException {
        Set<JavaMethod> successMethods = new HashSet<>();

        ClassLoader classLoader = createTestGenClassLoader(jproject);
        if (classLoader == null) {
            throw new TestGenException("Cannot create a class loader");
        }

        int num = 0;
        for (JavaMethod jmethod : methods) {
            try {
                runTest(jmethod, classLoader);
                successMethods.add(jmethod);
                num++;
            } catch (TestGenException e) {
                String[] mesg = e.getMessage().split("\n");
//                System.out.println(mesg[0] + " for a test method " + jmethod.getQualifiedName());
            }
        }
        if (num == 0) {
            throw new TestGenException("Cannot execute a test method");
        }
        return successMethods;
    }

    private static void runTest(JavaMethod jmethod, ClassLoader loader) throws TestGenException {
        JavaClass jclass = jmethod.getDeclaringClass();
        try {
            Class<?> clazz = Class.forName(jclass.getQualifiedName().fqn(), true, loader);
            JUnitCore junit = new JUnitCore();
            Request request = Request.method(clazz, jmethod.getName());

            Result result = junit.run(request);

            if (!result.wasSuccessful()) {
                StringBuilder errorMessage = new StringBuilder();
                for (Failure failure : result.getFailures()) {
                    errorMessage.append(" ").append(failure.getTrace());
                }
                throw new TestGenException("Cannot pass test: " + errorMessage.toString() + ".");
            }
        } catch (ClassNotFoundException e) {
            throw new TestGenException("Cannot execute a test method " + e.getMessage() + ".");
        }
    }

    private static TestGenClassLoader createTestGenClassLoader(JavaProject jproject) {
        try {
            URL[] urls = getURLs(jproject);

            return new TestGenClassLoader(urls);
        } catch (IOException e) {
            return null;
        }
    }

    private static URL[] getURLs(JavaProject jproject) throws IOException {
        String[] classpath = jproject.getClassPath();
        String binpath = jproject.getBinaryPath()[0];
        URL[] urls = new URL[classpath.length + 1];
        urls[0] = getURL(binpath);
        for (int index = 0; index < classpath.length; index++) {
            urls[index + 1] = getURL(classpath[index]);
        }
        return urls;
    }

    private static URL getURL(String path) throws IOException {
        File file;
        if (path.endsWith("/")) {
            file = new File(path);
        } else {
            file = new File(path + File.separatorChar);
        }
        return file.toURI().toURL();
    }
}
