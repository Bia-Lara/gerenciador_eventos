package br.ifsp.demo.exception;

public class DuplicateRegistrationException extends RuntimeException {
    public DuplicateRegistrationException() { super("User already registered in this event"); }
}