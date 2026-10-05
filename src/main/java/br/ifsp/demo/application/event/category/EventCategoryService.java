package br.ifsp.demo.application.event.category;

import br.ifsp.demo.application.event.category.CategoryRepository;
import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.application.event.EventRepository;
import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.dto.CreateCategoryRequest;
import br.ifsp.demo.exception.*;
import br.ifsp.demo.infrastructure.security.user.User;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

public class EventCategoryService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final CategoryRepository categoryRepository;
    private final Clock clock;

    public EventCategoryService(UserRepository userRepository,
                                EventRepository eventRepository,
                                RegistrationRepository registrationRepository,
                                CategoryRepository categoryRepository, Clock clock) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.categoryRepository = categoryRepository;
        this.clock = clock;
    }

    public void deleteCategory(UUID userId, UUID eventId, UUID categoryId) {
        validateIllegalArguments(userId, eventId, categoryId);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        if (!event.getStartDateTime().isAfter(LocalDateTime.now(clock))) {
            throw new EventAlreadyStartedException();
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (!event.getOrganizerId().equals(userId)) {
            throw new ActionNotAllowedException("Only the event organizer can delete categories");
        }

        Category category = event.findCategory(categoryId).orElseThrow(() -> new CategoryNotFoundException(categoryId));
        if (registrationRepository.existsByCategoryId(category.getId())) {
            throw new ActionNotAllowedException("Category has registrations");
        }

        event.removeCategory(category.getId());
        categoryRepository.delete(category);
    }


    public Category createCategory(CreateCategoryRequest request) {
        validateCreateCategoryIllegalArguments(request);

        Event event = eventRepository.findById(request.eventId()).orElseThrow(() -> new EventNotFoundException(request.eventId()));
        if (!event.getStartDateTime().isAfter(LocalDateTime.now(clock))) {
            throw new EventAlreadyStartedException();
        }

        userRepository.findById(request.organizerId()).orElseThrow(() -> new UserNotFoundException(request.organizerId()));

        if (!event.getOrganizerId().equals(request.organizerId())) {
            throw new ActionNotAllowedException("Only the event organizer can create categories");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        if (request.name().length() > 150) {
            throw new IllegalArgumentException("Category name must not exceed 150 characters");
        }

        if (request.price() < 0) {
            throw new IllegalArgumentException("Category price must not be negative");
        }

        Category category = event.addCategory(request.name(), request.capacity(), request.price());
        return categoryRepository.save(category);
    }

    private void validateCreateCategoryIllegalArguments(CreateCategoryRequest request) {
        if (request.eventId() == null) {
            throw new IllegalArgumentException("Event is required");
        }
        if (request.organizerId() == null) {
            throw new IllegalArgumentException("Organizer is required");
        }
    }

    private void validateIllegalArguments(UUID userId, UUID eventId, UUID categoryId) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event is required");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
    }
}
