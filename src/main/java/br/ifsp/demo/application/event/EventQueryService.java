package br.ifsp.demo.application.event;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class EventQueryService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public EventQueryService(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public List<Event> listByOrganizer(UUID organizerId) {
        if (organizerId == null) {
            throw new IllegalArgumentException("Organizer is required");
        }
        userRepository.findById(organizerId).orElseThrow(() -> new UserNotFoundException(organizerId));
        return eventRepository.findByOrganizerId(organizerId);
    }

    public List<Event> findByDate(LocalDate date) {
        Objects.requireNonNull(date, "Event date is required");
        return eventRepository.findByDate(date);
    }
}
