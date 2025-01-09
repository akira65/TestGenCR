package org.example.testgen_cr.generater;

import org.example.testgen_cr.model.TestGenParts;
import org.example.testgen_cr.model.VariableInfo;

import java.util.ArrayList;
import java.util.List;

public class Generater {
    public static List<String> generate(TestGenParts testGenParts) {
        List<String> testCodeList = new ArrayList<>();
        List<VariableInfo> returnInfoList = testGenParts.getReturnInfoList();
        for (int i = 0; i < returnInfoList.size(); i++) {
            StringBuilder testCode = new StringBuilder();

            testCode.append("package ").append(testGenParts.getPackageName()).append(System.lineSeparator());
            testCode.append("import static org.junit.Assert.*;").append(System.lineSeparator());
            testCode.append("import org.junit.Test;").append(System.lineSeparator());
            testCode.append(System.lineSeparator());

            // Class declaration
            testCode.append("public class ").append(testGenParts.getMethodName()).append("Test").append(i).append(System.lineSeparator());
            testCode.append(System.lineSeparator());
            // Test method
            testCode.append("    @Test").append(System.lineSeparator());
            testCode.append("    public void testTargetMethod() {").append(System.lineSeparator());

            // Variables initialization
            appendVariableInitialization(testCode, testGenParts.getVariableInfoList());

            // Fields initialization
            appendFieldInitialization(testCode, testGenParts.getFieldInfo());

            // Method invocation and assertions
            appendMethodInvocation(testCode, testGenParts, returnInfoList.get(i));

            testCode.append("    }").append(System.lineSeparator());
            testCode.append("}").append(System.lineSeparator());

            testCodeList.add(testCode.toString());
        }

        return testCodeList;
    }

    private static void appendVariableInitialization(StringBuilder testCode, List<VariableInfo> variables) {
        for (VariableInfo variable : variables) {
            testCode.append("        ")
                    .append(variable.getType()).append(" ")
                    .append(variable.getName()).append(" = ").append(variable.getValue()).append(";")
                    .append(System.lineSeparator());
        }
    }

    private static void appendFieldInitialization(StringBuilder testCode, List<VariableInfo> fields) {
        for (VariableInfo field : fields) {
            testCode.append("        ")
                    .append("// Assuming a target object instance is available")
                    .append(System.lineSeparator());
            testCode.append("        ").append(field.getType()).append(" ").append(field.getName().substring(field.getName().lastIndexOf("."))).append(" = ")
                    .append(field.getValue()).append(";")
                    .append(System.lineSeparator());
        }
    }

    private static void appendMethodInvocation(StringBuilder testCode, TestGenParts testGenParts, VariableInfo returnInfo) {
        String classQualifiedName = testGenParts.getTestGenMethod().getClassQualifiedName();
        String methodName = testGenParts.getMethodName();
        String returnType = returnInfo.getType();

        testCode.append("        ").append(classQualifiedName).append(" ").append("target = new ").append(classQualifiedName);
        testCode.append("        ")
                .append(returnType).append(" result = ")
                .append(methodName).append("(");


        List<VariableInfo> variables = testGenParts.getVariableInfoList();
        for (int i = 0; i < variables.size(); i++) {
            testCode.append(variables.get(i).getName());
            if (i < variables.size() - 1) {
                testCode.append(", ");
            }
        }
        testCode.append(");").append(System.lineSeparator());

        testCode.append("        assertEquals(")
                .append(returnInfo.getValue()).append(", result);")
                .append(System.lineSeparator());
    }

//    private String convertValue(String value, String type) {
//        if (value == null || "null".equals(value)) {
//            return "null";
//        }
//        switch (type) {
//            case "int":
//            case "long":
//            case "short":
//            case "byte":
//            case "float":
//            case "double":
//            case "boolean":
//            case "char":
//                return value;
//            case "String":
//                return "\"" + value + "\"";
//            default:
//                return "new " + type + "()"; // Fallback for complex types
//        }
//    }
}
