package br.ifsp.demo.exception;

public class UnauthorizedUserException extends RuntimeException {
    public UnauthorizedUserException() {
        this("User unauthorized");
    }

    public UnauthorizedUserException(String message) {
        super(message);
    }
}
