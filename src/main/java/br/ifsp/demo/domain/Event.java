package br.ifsp.demo.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Event {
    private final String name;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final UUID organizerId;
    private final List<Category> categories = new ArrayList<>();

    public Event(String name, LocalDateTime startDateTime, LocalDateTime endDateTime, UUID organizerId) {

        if (organizerId ==null){
            throw new IllegalArgumentException("Organizer is required");
        }

        this.name = name;
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

    public Category addCategory(String name, int capacity) {
        Category category = new Category(this, name, capacity);
        categories.add(category);
        return category;
    }

    public Optional<Category> findCategory(UUID categoryId) {
        return categories.stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst();
    }

    public List<Category> getCategories() {
        return List.copyOf(categories);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(name, event.name) && Objects.equals(startDateTime, event.startDateTime) && Objects.equals(endDateTime, event.endDateTime) && Objects.equals(organizerId, event.organizerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, startDateTime, endDateTime, organizerId);
    }


}
