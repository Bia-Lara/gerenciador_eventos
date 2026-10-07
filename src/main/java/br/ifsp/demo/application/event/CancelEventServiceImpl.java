package br.ifsp.demo.application.event;

import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.exception.EventNotFoundException;
import br.ifsp.demo.exception.UserNotFoundException;
import br.ifsp.demo.infrastructure.security.user.User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CancelEventServiceImpl implements CancelEventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

    public CancelEventServiceImpl(EventRepository eventRepository, UserRepository userRepository, RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public Event execute(UUID eventId, UUID userId) {
        Optional<User> user = userRepository.findById(userId);

        if(user.isEmpty()){
            throw new UserNotFoundException(userId);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        if (registrationRepository.existsActiveByEventId(eventId)) {
            throw new IllegalStateException("Cannot cancel event with active registrations");
        }

        event.cancel(userId);
        eventRepository.save(event);
        return event;
    }
}
