package br.ifsp.demo.exception;

public class UnauthorizedUserException extends RuntimeException {
    public UnauthorizedUserException() {
        super("User unauthorized");
    }
}
