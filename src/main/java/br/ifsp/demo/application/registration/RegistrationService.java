package br.ifsp.demo.application.registration;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.application.event.EventRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.exception.*;
import br.ifsp.demo.infrastructure.security.user.User;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final Clock clock;

    public RegistrationService(UserRepository userRepository,
                               EventRepository eventRepository,
                               RegistrationRepository registrationRepository,
                               Clock clock) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.clock = clock;
    }

    public Registration register(RegisterToEventRequest request) {
        validateIllegalArguments(request);
        Event event = eventRepository.findById(request.eventId()).orElseThrow(() -> new EventNotFoundException(request.eventId()));

        if (!event.getStartDateTime().isAfter(LocalDateTime.now(clock))) {
            throw new EventAlreadyStartedException();
        }

        User user = userRepository.findById(request.userId()).orElseThrow(() -> new UserNotFoundException(request.userId()));

        Category category = event.findCategory(request.categoryId()).orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        if (registrationRepository.existsActiveByUserIdAndEventId(request.userId(), request.eventId())) {
            throw new DuplicateRegistrationException();
        }
        if (registrationRepository.countActiveByCategoryId(category.getId()) >= category.getCapacity()) {
            throw new CategoryFullException();
        }

        Registration registration = new Registration(category, user);
        return registrationRepository.create(registration);
    }

    private void validateIllegalArguments(RegisterToEventRequest request) {
        if (request.eventId() == null) {
            throw new IllegalArgumentException("Event is required");
        }
        if (request.userId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (request.categoryId() == null) {
            throw new IllegalArgumentException("Category is required");
        }
    }
}
