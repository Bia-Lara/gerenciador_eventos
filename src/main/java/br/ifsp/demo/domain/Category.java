package br.ifsp.demo.domain;

import java.util.UUID;

public class Category {
    private static final int MAX_NAME_LENGTH = 150;

    private final Event event;
    private UUID id;
    private String name;
    private int capacity;
    private double price;

    public Category(Event event, String name, Integer capacity, Double price) {
        this.event = event;
        this.id = UUID.randomUUID();
        this.name = validateAndNormalizeName(name);
        this.capacity = validateCapacity(capacity);
        this.price = validatePrice(price);
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

    private String validateAndNormalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Category name must not exceed 150 characters");
        }

        return normalizedName;
    }

    private int validateCapacity(Integer capacity) {
        if (capacity == null) {
            throw new IllegalArgumentException("Category capacity is required");
        }

        if (capacity < 0) {
            throw new IllegalArgumentException("Category capacity must not be negative");
        }

        return capacity;
    }

    private double validatePrice(Double price) {
        if (price == null) {
            throw new IllegalArgumentException("Category price is required");
        }

        if (price < 0) {
            throw new IllegalArgumentException("Category price must not be negative");
        }

        return price;
    }
}
