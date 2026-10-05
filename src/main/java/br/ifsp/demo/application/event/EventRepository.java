package br.ifsp.demo.application.event;

import br.ifsp.demo.domain.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Optional<Event> findById(UUID id);
    Event save(Event event);
    List<Event> findByDate(LocalDate date);
    List<Event> findByOrganizerId(UUID organizerId);
}
