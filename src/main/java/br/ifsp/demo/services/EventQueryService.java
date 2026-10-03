package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.repository.EventRepository;
import br.ifsp.demo.domain.repository.UserRepository;

import java.util.List;
import java.util.UUID;

public class EventQueryService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public EventQueryService(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public List<Event> listByOrganizer(UUID organizerId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}