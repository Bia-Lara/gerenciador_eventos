package br.ifsp.demo.exception;

public class CategoryFullException extends RuntimeException {
    public CategoryFullException() { super("No vacancies left for this category"); }
}