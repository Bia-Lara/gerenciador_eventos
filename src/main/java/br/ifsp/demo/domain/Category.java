package br.ifsp.demo.domain;

import java.util.UUID;

public class Category {
    private final Event event;
    private UUID id;
    private String name;
    private int capacity;
    private double price;

    public Category(Event event, String name, int capacity, double price) {
        this.event = event;
        this.id = UUID.randomUUID();
        this.name = name;
        this.capacity = capacity;
        this.price = price;
    }

    public Event getEvent() {
        return event;
    }

    public UUID getId() {
        return id;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
