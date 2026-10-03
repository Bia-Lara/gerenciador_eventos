package br.ifsp.demo.exception;

import java.util.UUID;

public class EventNotFoundException extends EntityNotFoundException {
    public EventNotFoundException(UUID id) { super("Event not found: " + id); }
}