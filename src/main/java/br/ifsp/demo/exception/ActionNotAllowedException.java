package br.ifsp.demo.exception;

public class ActionNotAllowedException extends RuntimeException {
    public ActionNotAllowedException(String message) { super(message); }
}