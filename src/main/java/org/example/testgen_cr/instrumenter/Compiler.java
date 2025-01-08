package org.example.testgen_cr.instrumenter;

import org.example.testgen_cr.exception.TestGenException;
import org.jtool.srcmodel.JavaFile;
import org.jtool.srcmodel.JavaProject;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class Compiler {
    public static boolean run(JavaFile targetFile, JavaProject jproject) throws TestGenException {
        try {
            System.out.println(targetFile.getName() + "のコンパイル中です...");
            String classPathOption = "";
            String sourcePathOption = "";
            String binaryPathOption = "";

            try {
                if (jproject.getClassPath().length > 0) {
                    classPathOption = " -classpath " + getPathString(jproject.getClassPath());
                }
                if (jproject.getSourcePath().length > 0) {
                    sourcePathOption = " -sourcepath " + getPathString(jproject.getSourcePath());
                }
                if (!jproject.getBinaryPath()[0].isEmpty()) {
                    binaryPathOption = " -d " + getProjectBinDirPath(jproject);
                }
            } catch (Exception e) {
                throw new TestGenException("パスの処理中にエラーが発生しました" + e);
            }

            String targetOption = " -source " + 8 + " -target " + 8;
            String optionsStr = classPathOption + sourcePathOption + binaryPathOption + targetOption;

            String[] options = optionsStr.trim().split(" ");
            String[] args = new String[options.length + 1];
            System.arraycopy(options, 0, args, 0, options.length);

            ByteArrayOutputStream os1 = new ByteArrayOutputStream();
            ByteArrayOutputStream os2 = new ByteArrayOutputStream();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            args[options.length] = targetFile.getPath();

            try {
                int compileOk = compiler.run(null, os1, os2, args);
                if (compileOk == 0) {
                    return true;
                } else {
                    System.out.println(os1);
                    System.err.println(os2);
                    return false;
                }
            } catch (Exception e) {
                throw new TestGenException("コンパイル中にエラーが発生しました" + e);
            }
        } catch (Exception e) {
            throw new TestGenException("Compiler.runエラー" + e);
        }
    }

    private static String getPathString(String[] paths) throws TestGenException {
        try {
            StringBuilder sb = new StringBuilder();
            if (paths.length > 0) {
                sb.append(paths[0]);
                for (int i = 1; i < paths.length; i++) {
                    sb.append(File.pathSeparatorChar);
                    sb.append(paths[i]);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new TestGenException("パス文字列の生成中にエラーが発生しました" + e);
        }
    }

    private static String getProjectBinDirPath(JavaProject targetProject) throws TestGenException {
        try {
            for (String binPath : targetProject.getBinaryPath()) {
                if (binPath.contains(File.separator + "classes")) {
                    return binPath;
                }
            }
            return "";
        } catch (Exception e) {
            throw new TestGenException("バイナリディレクトリの取得中にエラーが発生しました" + e);
        }
    }
}
