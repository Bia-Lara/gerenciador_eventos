package br.ifsp.demo.exception;

import java.util.UUID;

public class CategoryNotFoundException extends EntityNotFoundException {
    public CategoryNotFoundException(UUID id) { super("Category not found: " + id); }
}
