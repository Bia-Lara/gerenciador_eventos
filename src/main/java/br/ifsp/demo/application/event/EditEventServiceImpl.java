package br.ifsp.demo.application.event;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.exception.EventNotFoundException;
import br.ifsp.demo.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EditEventServiceImpl implements EditEventService{
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EditEventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public Event execute(UUID eventId, UUID userId, String newName, LocalDateTime newStart, LocalDateTime newEnd) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        event.edit(userId, newName, newStart, newEnd);
        eventRepository.save(event);
        return event;
    }
}