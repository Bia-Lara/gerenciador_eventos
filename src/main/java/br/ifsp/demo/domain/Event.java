package br.ifsp.demo.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Event {
    private final UUID id;
    private final String name;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final UUID organizerId;
    private final List<Category> categories = new ArrayList<>();

    public Event(String name, LocalDateTime startDateTime, LocalDateTime endDateTime, UUID organizerId) {
        this.id = UUID.randomUUID();
        validateOrganizerId(organizerId);
        validatePeriod(startDateTime, endDateTime);

        this.name = validateAndNormalizeName(name);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.organizerId = organizerId;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public UUID getOrganizerId() {
        return organizerId;
    }

    public Category addCategory(String name, int capacity, double price) {
        Category category = new Category(this, name, capacity, price);
        categories.add(category);
        return category;
    }

    public void removeCategory(UUID categoryId) {
    }

    public Optional<Category> findCategory(UUID categoryId) {
        return categories.stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst();
    }

    private String validateAndNormalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Event name is required");
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 150) {
            throw new IllegalArgumentException("Event name must not exceed 150 characters");
        }

        return normalizedName;
    }

    private void validateOrganizerId(UUID organizerId) {
        if (organizerId == null) {
            throw new IllegalArgumentException("Organizer is required");
        }
    }

    private void validatePeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        if (startDateTime == null) {
            throw new IllegalArgumentException("Start date time is required");
        }
        if (endDateTime == null) {
            throw new IllegalArgumentException("End date time is required");
        }
        if (!endDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException("End date time must be after start date time");
        }
    }

    public List<Category> getCategories() {
        return List.copyOf(categories);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id) && Objects.equals(name, event.name) && Objects.equals(startDateTime, event.startDateTime) && Objects.equals(endDateTime, event.endDateTime) && Objects.equals(organizerId, event.organizerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, startDateTime, endDateTime, organizerId);
    }
}
