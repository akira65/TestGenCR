package org.example.testgen_cr.filter;

import java.util.ArrayList;
import java.util.List;


public class FilterResult {
    private List<String> notFoundInputVariables = new ArrayList<>();
    ;
    private List<String> notFoundOutputVariables = new ArrayList<>();
    ;
    private List<String> isNotObjectInput = new ArrayList<>();
    ;
    private List<String> isNotPrimitiveOutput = new ArrayList<>();
    ;

    public List<String> getNotFoundInputVariables() {
        return notFoundInputVariables;
    }

    public void addNotFoundInputVariable(String notFoundInputVariables) {
        this.notFoundInputVariables.add(notFoundInputVariables);
    }

    public List<String> getNotFoundOutputVariables() {
        return notFoundOutputVariables;
    }

    public void addNotFoundOutputVariable(String notFoundOutputVariables) {
        this.notFoundOutputVariables.add(notFoundOutputVariables);
    }

    public List<String> getIsNotObjectInput() {
        return isNotObjectInput;
    }

    public void addIsNotObjectInput(String isNotObjectInput) {
        this.isNotObjectInput.add(isNotObjectInput);
    }

    public List<String> getIsNotPrimitiveOutput() {
        return isNotPrimitiveOutput;
    }

    public void addIsNotPrimitiveOutput(String isNotPrimitiveOutput) {
        this.isNotPrimitiveOutput.add(isNotPrimitiveOutput);
    }
}
