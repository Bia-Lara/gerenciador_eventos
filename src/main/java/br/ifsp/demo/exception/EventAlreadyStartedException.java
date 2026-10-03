package br.ifsp.demo.exception;

public class EventAlreadyStartedException extends RuntimeException {
    public EventAlreadyStartedException() { super("Event has already started"); }
}
