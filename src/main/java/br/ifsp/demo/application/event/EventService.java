package br.ifsp.demo.application.event;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EventService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final Clock clock;

    public EventService(UserRepository userRepository, EventRepository eventRepository) {
        this(userRepository, eventRepository, Clock.systemDefaultZone());
    }

    public EventService(UserRepository userRepository, EventRepository eventRepository, Clock clock) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.clock = clock;
    }

    public Event createEvent(UUID organizerId, CreateEventRequest request) {
        validateIllegalArguments(organizerId, request);

        userRepository.findById(organizerId)
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));

        Event event = new Event(
                request.name(),
                request.startDateTime(),
                request.endDateTime(),
                organizerId
        );

        return eventRepository.save(event);
    }

    private void validateIllegalArguments(UUID organizerId, CreateEventRequest request) {
        if (organizerId == null) {
            throw new IllegalArgumentException("Organizer is required");
        }

        if (request.startDateTime() != null && !request.startDateTime().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("Start date time must be in the future");
        }
    }
}

