package org.example.testgen_cr.model;

public class VariableInfo {
    private final String name;
    private final String type;
    private final String value;

    public VariableInfo(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}