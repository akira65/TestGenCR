package org.example.testgen_cr.exception;

public class TestGenException extends Exception {

    private static final long serialVersionUID = 1L;

    private String message;

    public TestGenException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
