package br.ifsp.demo.exception;

import java.util.UUID;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(UUID id) { super("User not found: " + id); }
}