package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.repository.EventRepository;
import br.ifsp.demo.domain.repository.RegistrationRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import br.ifsp.demo.exception.EventAlreadyStartedException;
import br.ifsp.demo.exception.EventNotFoundException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

public class EventCategoryService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final Clock clock;

    public EventCategoryService(UserRepository userRepository,
                                EventRepository eventRepository,
                                RegistrationRepository registrationRepository,
                                Clock clock) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.clock = clock;
    }

    public void deleteCategory(UUID userId, UUID eventId, UUID categoryId) {
        validateIllegalArguments(userId, eventId, categoryId);

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        if (!event.getStartDateTime().isAfter(LocalDateTime.now(clock))) {
            throw new EventAlreadyStartedException();
        }

        throw new UnsupportedOperationException("Not implemented yet");
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