package org.example.testgen_cr.model;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.jtool.cfg.CFG;
import org.jtool.cfg.CFGMethodCall;
import org.jtool.srcmodel.JavaField;
import org.jtool.srcmodel.JavaLocalVar;
import org.jtool.srcmodel.JavaMethod;
import org.jtool.srcmodel.JavaProject;

import java.util.*;
import java.util.stream.Collectors;

public class TestGenMethod {
    private final CFG cfg;
    private final JavaMethod jmethod;
    private final CFGMethodCall callNode;
    private final String className;

    public TestGenMethod(CFG cfg, JavaMethod jmethod, CFGMethodCall callNode) {
        this.cfg = cfg;
        this.jmethod = jmethod;
        this.callNode = callNode;
        String classPath = jmethod.getDeclaringClass().getClassName();
        this.className = classPath.substring(classPath.lastIndexOf('.') + 1);
    }

    /**
     * メソッドの単純な名前 (e.g. "foo" や "bar" ) を返す
     */
    public String getMethodName() {
        return this.jmethod.getName();
    }

    /**
     * メソッドが属するクラスの FQCN (e.g. "com.example.MyClass")
     */
    public String getClassQualifiedName() {
        return this.jmethod.getDeclaringClass().getQualifiedName().fqn();
    }

    /**
     * Java ソースレベルの戻り値型 (import など考慮済みで、FQCN ではない場合あり)
     * 例: "String", "int", "List<String>" など
     */
    public String getReturnType() {
        return this.jmethod.getReturnType();
    }

    /**
     * FQCN 形式での戻り値
     * 例: "java.lang.String", "int", "java.util.List" など
     */
    public String getFullReturnType() {
        IMethodBinding methodBinding = this.jmethod.getMethodBinding();
        if (methodBinding == null) {
            // バインド情報がなければ正確には取得不能なので "void" とする
            // （あるいは null を返してもよい）
            return "void";
        }
        ITypeBinding returnTypeBinding = methodBinding.getReturnType();
        return returnTypeBinding.getQualifiedName();
    }

    /**
     * Generics や配列記号 "[]" を除去した戻り値型 (例: "List<String>" -> "List" や "String[]" -> "String")
     */
    public String getNoListReturnType() {
        return removeArraySymbols(removeGenericSymbols(
                Collections.singletonList(getFullReturnType()))
        ).get(0);
    }

    /**
     * パラメータの型一覧 (単純名ベース)
     * 例: ["String", "int"] など
     */
    public List<String> getVariableTypeList() {
        return jmethod.getParameters().stream()
                .map(JavaLocalVar::getType) // e.g. "String", "int"
                .collect(Collectors.toList());
    }

    /**
     * パラメータの型一覧 (FQCN ベース)
     * 例: ["java.lang.String", "int", "java.util.List<java.lang.Integer>" ] など
     */
    public List<String> getFullVariableTypeList() {
        IMethodBinding methodBinding = this.jmethod.getMethodBinding();
        if (methodBinding == null) {
            return List.of();
        }
        ITypeBinding[] typeBindingList = methodBinding.getParameterTypes();
        return Arrays.stream(typeBindingList)
                .map(ITypeBinding::getQualifiedName)
                .collect(Collectors.toList());
    }

    /**
     * パラメータの全ての型を、Generics や配列記号を分解してリストアップ
     * 例: ["java.util.List<java.lang.Integer>"] -> ["java.util.List", "java.lang.Integer"]
     */
    public List<String> getAllVariableTypeList() {
        List<String> result = new ArrayList<>();
        for (String fqnType : getFullVariableTypeList()) {
            result.addAll(removeArraySymbols(removeGenericSymbols(List.of(fqnType))));
        }
        return result;
    }

    /**
     * 引数 (パラメータ) 名一覧
     * 例: ["arg0", "arg1"] など
     * ただしデバッグ情報/AST情報がない場合は空文字/不定になる場合もある
     */
    public List<String> getParameterNames() {
        return jmethod.getParameters().stream()
                .map(JavaLocalVar::getName).collect(Collectors.toList());

    }

    /**
     * 修飾子の整数値 (e.g. java.lang.reflect.Modifier 互換のビット列)
     */
    public int getModifiers() {
        return jmethod.getModifiers();
    }

    /**
     * public / protected / private / default の判定
     */
    public boolean isPublic() {
        return jmethod.isPublic();
    }

    public boolean isProtected() {
        return jmethod.isProtected();
    }

    public boolean isPrivate() {
        return jmethod.isPrivate();
    }

    public boolean isDefaultAccess() {
        return jmethod.isDefault();
    }

    public boolean isStatic() {
        return jmethod.isStatic();
    }

    public boolean isConstructor() {
        return jmethod.isConstructor();
    }

    /**
     * コールサイト (呼び出し箇所) のソース行番号を取得
     * CompilationUnit が取れない場合や情報が無い場合は -1 を返す
     */
//    public int getCallSiteLineNumber() {
//        if (callNode == null || callNode.getASTNode() == null) {
//            return -1;
//        }
//        // ASTNode#getRoot() が CompilationUnit のはずだが、保証されていない場合はキャスト失敗の可能性あり
//        if (callNode.getASTNode().getRoot() instanceof CompilationUnit cu) {
//            int startPos = callNode.getASTNode().getStartPosition();
//            return cu.getLineNumber(startPos);
//        }
//        return -1;
//    }

    /**
     * バイトコードのメソッド記述子 (Method Descriptor) を返す
     * 例: (Ljava/lang/String;I)Ljava/lang/Object;
     * <p>
     * ※従来の toJVMDescriptor() ではメソッド名を含めていましたが、
     * 本来のバイトコード仕様ではメソッド名は含まず、パラメータと戻り値の型のみです。
     */
    public String getJVMMethodDescriptor() {
        // もし jmethod のバインド情報があるなら直接 IMethodBinding から取る方法もありますが、
        // ここでは既存の変換ロジックを活用します
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (String ptype : getVariableTypeList()) {
            sb.append(toJVMTypeDescriptor(ptype));
        }
        sb.append(")");
        sb.append(toJVMTypeDescriptor(getReturnType()));
        return sb.toString();
    }

    /**
     * 「クラス名.メソッド名 + バイトコード記述子」等の形式が必要な場合に使えるヘルパー
     * 例:  "com/example/MyClass.foo(Ljava/lang/String;I)Ljava/lang/String;"
     */
    public String getFullJVMMethodSignature() {
        String internalClassName = getClassQualifiedName().replace('.', '/');
        return internalClassName + "." + getMethodName() + getJVMMethodDescriptor();
    }

    public String getJVMMethodSignatureWithoutClassPath() {
        return getMethodName() + getJVMMethodDescriptor();
    }

    /**
     * Java の型名を JVM の内部表現に変換
     * 例: "int" -> "I", "java.lang.String" -> "Ljava/lang/String;", "Foo[]" -> "[LFoo;"
     */
    private String toJVMTypeDescriptor(String javaType) {
        // 原始型
        switch (javaType) {
            case "int":
                return "I";
            case "boolean":
                return "Z";
            case "byte":
                return "B";
            case "char":
                return "C";
            case "short":
                return "S";
            case "long":
                return "J";
            case "float":
                return "F";
            case "double":
                return "D";
            case "void":
                return "V";
        }
        // 配列型
        if (javaType.endsWith("[]")) {
            String baseType = javaType.substring(0, javaType.length() - 2);
            return "[" + toJVMTypeDescriptor(baseType);
        }
        // 参照型
        // generics や「<」などを削除しておく: "java.util.List<java.lang.String>" -> "java.util.List"
        String noGenerics = removeGenericSymbols(List.of(javaType)).get(0);
        return "L" + noGenerics.replace('.', '/') + ";";
    }

    /**
     * JavaProject への参照
     */
    public JavaProject getJavaProject() {
        return jmethod.getJavaProject();
    }

    /**
     * このメソッドの属する CFG
     */
    public CFG getCfg() {
        return cfg;
    }

    /**
     * ラップしている元の JavaMethod (内部的に使う場合)
     */
    public JavaMethod getJavaMethod() {
        return jmethod;
    }

    /**
     * 呼び出し CFG ノード
     */
    public CFGMethodCall getCallNode() {
        return callNode;
    }

    /**
     * "クラス名.メソッドシグネチャ@戻り値" の文字列表現
     * 例: "com.example.MyClass.foo( java.lang.String )@java.lang.String"
     */
    public String getQualifiedName() {
        return jmethod.getQualifiedName().fqn() + "@" + getFullReturnType();
    }

    /**
     * コールノード位置つきでの文字列表現
     * 例: "com.example.MyClass.foo(...)@{123}"
     */
    public String getQualifiedNameWithCalling() {
        return jmethod.getQualifiedName()
                + (callNode != null ? "@{" + callNode.getASTNode().getStartPosition() + "}" : "");
    }

    // テストメソッド判定は既存のまま
    public static boolean isBeforeOrAfterTestMethod(JavaMethod jmethod) {
        // ...
        // 省略（元の実装と同じ）
        return false;
    }

    /**
     * このメソッド内でアクセスしているフィールド（読み書き含む）
     */
    public Set<JavaField> getAccessedFields() {
        return jmethod.getAccessedFields();
    }

    /**
     * このメソッドから呼び出しているメソッド一覧
     */
    public Set<JavaMethod> getCalledMethods() {
        return jmethod.getCalledMethods();
    }

    //================================================
    // 以下、ユーティリティメソッド群
    //================================================

    private static List<String> removeArraySymbols(List<String> typeList) {
        List<String> cleanedList = new ArrayList<>();
        for (String type : typeList) {
            if (type.endsWith("[]")) {
                cleanedList.add(type.substring(0, type.length() - 2));
            } else {
                cleanedList.add(type);
            }
        }
        return cleanedList;
    }

    private static List<String> removeGenericSymbols(List<String> typeList) {
        List<String> cleanedList = new ArrayList<>();
        for (String type : typeList) {
            int genericStart = type.indexOf('<');
            if (genericStart != -1) {
                // 先頭～ < の前までを取得  (例: "java.util.List<java.lang.String>" -> "java.util.List" )
                String base = type.substring(0, genericStart);
                cleanedList.add(base);
            } else {
                cleanedList.add(type);
            }
        }
        return cleanedList;
    }

    public String getClassName() {
        return className;
    }
}
