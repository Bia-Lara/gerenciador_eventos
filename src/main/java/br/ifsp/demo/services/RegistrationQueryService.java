package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.enumerations.RegistrationFilter;
import br.ifsp.demo.domain.repository.RegistrationRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import br.ifsp.demo.exception.UserNotFoundException;

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
        validateIllegalArguments(userId, filter);

        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return List.of();
    }

    private void validateIllegalArguments(UUID userId, RegistrationFilter filter) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (filter == null) {
            throw new IllegalArgumentException("Filter is required");
        }
    }
}
