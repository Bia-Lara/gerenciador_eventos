package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.enumerations.RegistrationFilter;
import br.ifsp.demo.domain.repository.RegistrationRepository;
import br.ifsp.demo.domain.repository.UserRepository;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

public class RegistrationQueryService {
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;
    private final Clock clock;

    public RegistrationQueryService(UserRepository userRepository,
                                    RegistrationRepository registrationRepository,
                                    Clock clock) {
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.clock = clock;
    }

    public List<Event> listEventsByUser(UUID userId, RegistrationFilter filter) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
