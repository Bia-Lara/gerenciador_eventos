package br.ifsp.demo.exception;

public class NullValueException extends RuntimeException {
    public NullValueException(String fieldName) {
        super(fieldName + " is required");
    }
}
