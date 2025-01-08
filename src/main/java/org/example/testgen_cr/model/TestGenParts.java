package org.example.testgen_cr.model;

import java.util.ArrayList;
import java.util.List;

public class TestGenParts {
    private TestGenMethodSeq methodSeq;
    private TestGenMethod evoTargetMethod;
    private TestGenMethod testGenMethod;
    private List<VariableInfo> variableInfoList = new ArrayList<>();
    private List<VariableInfo> fieldInfoList = new ArrayList<>();
    private List<VariableInfo> returnInfoList = new ArrayList<>();

    public TestGenParts(TestGenMethodSeq methodSeq) {
        this.methodSeq = methodSeq;
        this.evoTargetMethod = methodSeq.getEvoTargetMethod();
        this.testGenMethod = methodSeq.getTestGenTargetMethod();
    }

    public TestGenMethodSeq getMethodSeq() {
        return methodSeq;
    }

    public TestGenMethod getEvoTargetMethod() {
        return evoTargetMethod;
    }

    public TestGenMethod getTestGenMethod() {
        return testGenMethod;
    }

    public void setVariableInfo(String name, String type, String value) {
        this.variableInfoList.add(new VariableInfo(name, type, value));
    }

    public List<VariableInfo> getVariableInfoList() {
        return variableInfoList;
    }

    public void setFieldInfo(String name, String type, String value) {
        this.fieldInfoList.add(new VariableInfo(name, type, value));
    }

    public List<VariableInfo> getFieldInfo() {
        return fieldInfoList;
    }

    public void setReturnInfo(String name, String type, String value) {
        System.out.println("ReturnName: " + name);
        this.returnInfoList.add(new VariableInfo(name, type, value));
    }

    public List<VariableInfo> getReturnInfoList() {
        return returnInfoList;
    }

    public String getPackageName() {
        return methodSeq.getClass().getName();
    }

    public String getMethodName() {
        return testGenMethod.getMethodName();
    }
}
