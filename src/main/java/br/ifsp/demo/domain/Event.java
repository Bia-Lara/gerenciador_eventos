package br.ifsp.demo.domain;

import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.exception.*;

import java.time.LocalDateTime;
import java.util.*;

public class Event {
    private UUID id;
    private final String name;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final UUID organizerId;
    private final List<Category> categories = new ArrayList<>();
    private EventStatus status = EventStatus.ACTIVE;

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

    public Category addCategory(String name, Integer capacity, Double price) {
        Category category = new Category(this, name, capacity, price);
        validateCategoryNameAvailability(category.getName());
        categories.add(category);
        return category;
    }

    public void ensureNotStarted(LocalDateTime now) {
        if (!startDateTime.isAfter(now)) {
            throw new EventAlreadyStartedException();
        }
    }

    public void ensureOrganizerCanCreateCategory(UUID organizerId) {
        if (!this.organizerId.equals(organizerId)) {
            throw new ActionNotAllowedException("Only the event organizer can create categories");
        }
    }

    public void removeCategory(UUID categoryId) {
        categories.removeIf(c -> c.getId().equals(categoryId));
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

    private void validateCategoryNameAvailability(String categoryName) {
        if (categories.stream().anyMatch(category -> category.getName().equals(categoryName))) {
            throw new EntityAlreadyExistsException("Category name already exists");
        }
    }

    private void ensureIsOrganizer(UUID organizerId){
        if(!this.organizerId.equals(organizerId)){
            throw new UnauthorizedUserException();
        }
    }

    public void cancel(UUID requestingUserId) {
        if (requestingUserId == null) {
            throw new NullValueException("User");
        }

        ensureIsOrganizer(requestingUserId);

        ensureNotStarted(LocalDateTime.now());

        if (this.status == EventStatus.CANCELLED) {
            throw new IllegalStateException("Event is already cancelled");
        }

        this.status = EventStatus.CANCELLED;
    }

    public void edit(UUID requestingUserId, String newName, LocalDateTime newStartDateTime, LocalDateTime newEndDateTime) {
        if (requestingUserId == null) {
            throw new NullValueException("User");
        }

        ensureIsOrganizer(requestingUserId);

        validatePeriod(newStartDateTime, newEndDateTime);

        validateAndNormalizeName(newName);

        if (!newStartDateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date time must be after now");
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

    public static Event restore(UUID id, String name, LocalDateTime start, LocalDateTime end, UUID organizerId) {
        Event event = new Event(name, start, end, organizerId);
        event.id = id;
        return event;
    }

    public Category restoreCategory(UUID id, String name, Integer capacity, Double price) {
        Category category = addCategory(name, capacity, price);
        category.setId(id);
        return category;
    }

    public EventStatus getStatus() {
        return status;
    }
}
