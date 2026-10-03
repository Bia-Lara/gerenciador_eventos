package br.ifsp.demo.domain;

import java.util.UUID;

public class Category {
    private final Event event;
    private final UUID uuid;
    private String name;
    private int capacity;

    public Category(Event event, String name, int capacity) {
        this.event = event;
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.capacity = capacity;
    }

    public Event getEvent() {
        return event;
    }

    public UUID getId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
