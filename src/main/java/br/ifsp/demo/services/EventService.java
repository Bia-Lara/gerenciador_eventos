package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.repository.OrganizerRepository;
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.exception.EntityNotFoundException;

public class EventService {
    private final OrganizerRepository organizerRepository;

    public EventService(OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
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
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Event name is required");
        }
    }
}
