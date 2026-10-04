package br.ifsp.demo.application.event;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.application.organizer.OrganizerRepository;
import br.ifsp.demo.exception.EntityNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class EventService {
    private final OrganizerRepository organizerRepository;
    private final EventRepository eventRepository;
    private final Clock clock;

    public EventService(OrganizerRepository organizerRepository) {
        this(organizerRepository, null, Clock.systemDefaultZone());
    }

    public EventService(OrganizerRepository organizerRepository, Clock clock) {
        this(organizerRepository, null, clock);
    }

    public EventService(OrganizerRepository organizerRepository, EventRepository eventRepository, Clock clock) {
        this.organizerRepository = organizerRepository;
        this.eventRepository = eventRepository;
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

    public List<Event> findByDate(LocalDate date) {
        Objects.requireNonNull(date, "Event date is required");

        return eventRepository.findByDate(date);
    }

    private void validateIllegalArguments(CreateEventRequest request) {
        if (request.organizerId() == null) {
            throw new IllegalArgumentException("Organizer is required");
        }

        if (request.startDateTime() != null && !request.startDateTime().isAfter(LocalDateTime.now(clock))) {
            throw new IllegalArgumentException("Start date time must be in the future");
        }
    }
}
