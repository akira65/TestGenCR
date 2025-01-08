package org.example.testgen_cr.filter;

import org.example.testgen_cr.model.TestGenMethod;
import org.example.testgen_cr.model.TestGenMethodSeq;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MethodFilter {
    private FilterResult filterResult = new FilterResult();
    private static final List<String> PRIMITIVE_TYPES = List.of(
            "byte", "short", "int", "long", "float", "double", "boolean", "char", "String"
    );

    private List<TestGenMethodSeq> targetMethodSeqList = new ArrayList<>();

    public MethodFilter(List<TestGenMethodSeq> methodSeqList) {
        for (TestGenMethodSeq seq : methodSeqList) {
            TestGenMethod targetMethod = seq.getTestGenTargetMethod();
            List<String> valiableTypeList = targetMethod.getAllVariableTypeList();
            String returnType = targetMethod.getNoListReturnType();

            if (isTarget(targetMethod.getQualifiedName(), valiableTypeList, returnType)) {
                targetMethodSeqList.add(methodSeqList.get(methodSeqList.lastIndexOf(seq)));
            }

        }
    }

    private boolean isTarget(String qualifiedName, List<String> valiableTypeList, String returnType) {
        boolean isTargetFlag = true;

        for (String variableType : valiableTypeList) {
            if (!isPrimitive(variableType)) {
                isTargetFlag = true;
                break;
            }
            isTargetFlag = false;
        }

        if (!isTargetFlag) {
            filterResult.addIsNotObjectInput(qualifiedName);
        }

        if (valiableTypeList.isEmpty()) {
            filterResult.addNotFoundInputVariable(qualifiedName);
            isTargetFlag = false;
        }

        if (Objects.equals(returnType, "void")) {
            filterResult.addNotFoundOutputVariable(qualifiedName);
            isTargetFlag = false;
        }

        if (!isPrimitive(returnType)) {
            filterResult.addIsNotPrimitiveOutput(qualifiedName);
            isTargetFlag = false;
        }

        return isTargetFlag;
    }

    private boolean isPrimitive(String type) {
        String simpleType = type.substring(type.lastIndexOf(".") + 1);
        return PRIMITIVE_TYPES.contains(simpleType);
    }

    public List<TestGenMethodSeq> getTargetMethodSeqList() {
        return targetMethodSeqList;
    }

    public FilterResult getFilterResult() {
        return filterResult;
    }
}
