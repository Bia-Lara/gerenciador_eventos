package br.ifsp.demo.domain.repository;

import br.ifsp.demo.domain.Event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {
    Optional<Event> findById(UUID id);
    List<Event> findByOrganizerId(UUID organizerId);
}
