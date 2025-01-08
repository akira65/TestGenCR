package org.example.testgen_cr.instrumenter;

import javassist.*;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.example.testgen_cr.model.TestGenMethod;
import org.example.testgen_cr.model.TestGenMethodSeq;
import org.jtool.srcmodel.JavaClass;
import org.jtool.srcmodel.JavaField;
import org.jtool.srcmodel.JavaProject;

import java.io.File;
import java.util.List;
import java.util.Set;

public class LogCodeInserter {

    private static final String LOG_FILE_PATH = "/Users/hara_ko/Desktop/TestGenCR/testGenTarget/injectedLogs.txt";

    public boolean insertLogCode(TestGenMethodSeq seq, JavaProject project) throws Exception {
        int lastIndex = seq.getMethodChainSize() - 1;
        if (lastIndex < 0) {
            System.err.println("No methods in chain.");
            return false;
        }
        TestGenMethod lastMethod = seq.getSeqMethod(lastIndex);

        String classQualifiedName = lastMethod.getClassQualifiedName();
        String methodName = lastMethod.getMethodName();
        String jvmDescriptor = lastMethod.getJVMMethodDescriptor();

        ClassPool pool = createClassPool(project);

        CtClass ctClass;
        try {
            ctClass = pool.get(classQualifiedName);
        } catch (NotFoundException e) {
            System.err.println("Class not found for: " + classQualifiedName + " (need compile?)");
            return false;
        }

        CtMethod targetMethod = null;
        for (CtMethod m : ctClass.getDeclaredMethods(methodName)) {
            System.out.println("================================");
            System.out.println("Method: " + m.getName());
            System.out.println("Descriptor: " + m.getMethodInfo().getDescriptor());
            if (m.getMethodInfo().getDescriptor().equals(jvmDescriptor)) {
                targetMethod = m;
                break;
            }
        }

        String qualifiedName = classQualifiedName + "#" + methodName + jvmDescriptor;

        if (targetMethod == null) {
            System.err.println("Target method not found: " + qualifiedName);
            return false;
        }

        String logCode = generateLogCode(lastMethod, qualifiedName);
        if (logCode != null) {
            System.out.println("Generated log code:\n" + logCode);
            System.out.println(getProjectBinDirPath(project));

            targetMethod.insertBefore(logCode);
            System.out.println("inserted log code:\n" + logCode);
            instrumentMethodCall(targetMethod, seq.getCalleeMethod(lastMethod).getMethodName());
            System.out.println("lllllllllll");
            ctClass.writeFile(getProjectBinDirPath(project));
            ctClass.detach();

            System.out.println("=== Successfully injected log code into " + classQualifiedName + "#" + methodName + " ===");
            return true;
        }

        return false;
    }

    private ClassPool createClassPool(JavaProject project) throws NotFoundException {
        ClassPool pool = new ClassPool(true);

        if (project.getClassPath() != null) {
            for (String cp : project.getClassPath()) {
                pool.insertClassPath(cp);
            }
        }
        if (project.getSourcePath() != null) {
            for (String src : project.getSourcePath()) {
                pool.insertClassPath(src);
            }
        }
        if (project.getBinaryPath() != null) {
            for (String bin : project.getBinaryPath()) {
                pool.insertClassPath(bin);
            }
        }

        return pool;
    }

    private String generateLogCode(TestGenMethod testGenMethod, String qualifiedName) {
        StringBuilder sb = new StringBuilder();

        // メソッド引数のログ出力コード生成
        List<String> paramNames = testGenMethod.getParameterNames();
        List<String> paramTypes = testGenMethod.getVariableTypeList();
        sb.append("System.out.println(\"TESTGEN_LOGCODELogInfoStart\");\n");
        sb.append("System.out.println(\"TESTGEN_LOGCODE").append(qualifiedName).append(": {\");\n");

        if (paramNames != null && !paramNames.isEmpty() && paramTypes != null && !paramTypes.isEmpty()) {
            sb.append("System.out.println(\"TESTGEN_LOGCODE  args: {\");\n");

            for (int i = 0; i < paramNames.size(); i++) {
                String pName = paramNames.get(i);
                String pType = paramTypes.get(i);
                int paramIndex = i + 1; // Javassistでは引数は1から始まる

                if (pType.equals("boolean") ||
                        pType.equals("byte") || pType.equals("short") || pType.equals("int") ||
                        pType.equals("long") || pType.equals("float") || pType.equals("double") ||
                        pType.equals("char") || pType.equals("java.lang.String") ||
                        pType.equals("java.lang.Boolean") || pType.equals("java.lang.Byte") ||
                        pType.equals("java.lang.Short") || pType.equals("java.lang.Integer") ||
                        pType.equals("java.lang.Long") || pType.equals("java.lang.Float") ||
                        pType.equals("java.lang.Double") || pType.equals("java.lang.Character") ||
                        pType.startsWith("java.time.")) {
                    // プリミティブ型はそのまま出力
                    sb.append("System.out.println(\"TESTGEN_LOGCODE    ").append(pName).append(": ").append(pType).append(": \" + $").append(paramIndex).append(");\n");
                } else {
                    // オブジェクト型は例外処理を伴う出力
                    sb.append("    System.out.println(\"TESTGEN_LOGCODE    ").append(pName).append(": ").append(pType).append(": Object\");\n");
                }
            }
            sb.append("System.out.println(\"TESTGEN_LOGCODE  }\");\n");
        }

        Set<JavaField> accessedFields = testGenMethod.getAccessedFields();

        if (accessedFields != null && !accessedFields.isEmpty() && !testGenMethod.isStatic()) {
            sb.append("System.out.println(\"TESTGEN_LOGCODE  fields: {\");\n");

            for (JavaField field : accessedFields) {
                String fieldName = field.getName();
                String fieldType = field.getType();
                JavaClass declaringClass = field.getDeclaringClass(); // 所有クラスを取得
                String ownerReference;

                // 所有クラスがテスト対象クラスと一致する場合は `this` を使用
                if (declaringClass.equals(testGenMethod.getJavaMethod().getDeclaringClass())) {
                    ownerReference = "this";
                } else {
                    // テスト対象クラス外の場合、完全修飾クラス名を使用
                    ownerReference = declaringClass.getClassName();
                }

                if (fieldType.equals("boolean") ||
                        fieldType.equals("byte") || fieldType.equals("short") || fieldType.equals("int") ||
                        fieldType.equals("long") || fieldType.equals("float") || fieldType.equals("double") ||
                        fieldType.equals("char") || fieldType.equals("java.lang.String") ||
                        fieldType.equals("java.lang.Boolean") || fieldType.equals("java.lang.Byte") ||
                        fieldType.equals("java.lang.Short") || fieldType.equals("java.lang.Integer") ||
                        fieldType.equals("java.lang.Long") || fieldType.equals("java.lang.Float") ||
                        fieldType.equals("java.lang.Double") || fieldType.equals("java.lang.Character") ||
                        fieldType.startsWith("java.time.")) {
                    // プリミティブ型はそのまま出力
                    sb.append("System.out.println(\"TESTGEN_LOGCODE    ").append(ownerReference).append(".").append(fieldName).append(": ").append(fieldType).append(": \" + ").append(ownerReference).append(".").append(fieldName).append(");\n");
                } else {
                    // その他の型は例外処理を伴う出力
                    sb.append("System.out.println(\"TESTGEN_LOGCODE    ").append(ownerReference).append(".").append(fieldName).append(": ").append(fieldType).append(": Object\");\n");
                }
            }
            sb.append("System.out.println(\"TESTGEN_LOGCODE  }\");\n");
        }


        // 引数もフィールドも存在しない場合はnullを返す
        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }

    private void instrumentMethodCall(CtMethod ctmethod, String targetMethodName) throws CannotCompileException {
        ctmethod.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getMethodName().equals(targetMethodName)) {
                    try {
                        String returnType = m.getMethod().getReturnType().getName();
                        // 呼び出しのコンテキストを解析
                        if (isInReturnStatement(m)) {
                            // return ステートメント内で直接使用されている場合
                            m.replace("$_ = $proceed($$);\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE  return: {\");\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE    " + targetMethodName + ": " + returnType + ": \" + $_);\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE  }\");\n" +
                                    "return $_;\n");
                        } else if (isAssigned(m)) {
                            // 代入文の場合
                            m.replace("$proceed($$);\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE  return: {\");\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE    " + targetMethodName + ": " + returnType + ": \" + $_);\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE  }\");\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE}\");\n");
                        } else {
                            // 代入文でない場合は一時変数を使用
                            m.replace("$_ = $proceed($$);\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE  return: {\");\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE    " + targetMethodName + ": " + returnType + ": \" + $_);\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE  }\");\n" +
                                    "System.out.println(\"TESTGEN_LOGCODE}\");\n");
                        }
                    } catch (NotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private boolean isInReturnStatement(MethodCall m) {
        try {
            CodeIterator iterator = m.where().getMethodInfo().getCodeAttribute().iterator();
            // バイトコードを前進させて次の命令を取得
            while (iterator.hasNext()) {
                int nextOpCodePos = iterator.next();
                int nextOpCode = iterator.byteAt(nextOpCodePos);

                // `return`命令（ARETURN, IRETURN, FRETURN, DRETURN, LRETURN, RETURN）を確認
                if (nextOpCode == Opcode.ARETURN ||
                        nextOpCode == Opcode.IRETURN ||
                        nextOpCode == Opcode.FRETURN ||
                        nextOpCode == Opcode.DRETURN ||
                        nextOpCode == Opcode.LRETURN ||
                        nextOpCode == Opcode.RETURN) {
                    return true; // `return`命令が見つかった
                }

                // 他の命令があれば`return`でないと判断
                if (nextOpCode != Opcode.NOP) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error determining if in return statement: " + e.getMessage());
        }
        return false;
    }


    public boolean isAssigned(MethodCall m) {
        CodeIterator iterator = m.where().getMethodInfo().getCodeAttribute().iterator();
        int pos = m.indexOfBytecode();

        // メソッド呼び出し後の命令を取得
        int nextOpCodePos = iterator.lookAhead();
        int nextOpCode = iterator.byteAt(nextOpCodePos);

        // 代入命令（store命令）を判定
        return isStoreInstruction(nextOpCode);
    }

    private boolean isStoreInstruction(int opCode) {
        // バイトコード命令が store 系（代入）であるかを判定
        return (opCode >= Opcode.ISTORE && opCode <= Opcode.ASTORE);
    }

    private String getProjectBinDirPath(JavaProject targetProject) {
        for (String binPath : targetProject.getBinaryPath()) {
            if (binPath.contains(File.separatorChar + "classes")) {
                return binPath;
            }
        }
        return "";
    }
}
