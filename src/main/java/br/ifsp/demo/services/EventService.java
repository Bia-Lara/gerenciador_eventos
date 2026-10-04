package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.repository.OrganizerRepository;
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.exception.EntityNotFoundException;

import java.time.Clock;
import java.time.LocalDateTime;

public class EventService {
    private final OrganizerRepository organizerRepository;
    private final Clock clock;

    public EventService(OrganizerRepository organizerRepository) {
        this(organizerRepository, Clock.systemDefaultZone());
    }

    public EventService(OrganizerRepository organizerRepository, Clock clock) {
        this.organizerRepository = organizerRepository;
        this.clock = clock;
    }

    public Event createEvent(CreateEventRequest request) {
        validateIllegalArguments(request);

        organizerRepository.findById(request.organizerId())
                .orElseThrow(() -> new EntityNotFoundException("Organizer not found"));

        return new Event(
                request.name(),
                request.startDateTime(),
                request.endDateTime(),
                request.organizerId()
        );
    }

    private void validateIllegalArguments(CreateEventRequest request) {
        if (request.organizerId() == null) {
            throw new IllegalArgumentException("Organizer is required");
        }

        if (request.startDateTime() !=null && request.startDateTime().isBefore(LocalDateTime.now(clock))){
            throw new IllegalArgumentException("Start date time must be in the future");
        }
    }
}
