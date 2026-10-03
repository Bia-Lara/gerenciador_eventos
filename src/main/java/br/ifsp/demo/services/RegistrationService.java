package br.ifsp.demo.services;

import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.repository.EventRepository;
import br.ifsp.demo.domain.repository.RegistrationRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import br.ifsp.demo.dto.RegisterToEventRequest;
import br.ifsp.demo.exception.EventNotFoundException;

import java.time.Clock;

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
        eventRepository.findById(request.eventId()).orElseThrow(() -> new EventNotFoundException(request.eventId()));

        throw new UnsupportedOperationException("Not implemented yet");
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