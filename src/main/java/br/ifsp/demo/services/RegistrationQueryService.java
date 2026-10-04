package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationFilter;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.domain.repository.RegistrationRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import br.ifsp.demo.exception.UserNotFoundException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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

        LocalDateTime now = LocalDateTime.now(clock);
        Stream<Registration> registrations = registrationRepository.findByUserId(userId).stream();

        if (filter == RegistrationFilter.ATIVOS) {
            registrations = registrations.filter(r -> isActive(r, now));
        } else if (filter == RegistrationFilter.INATIVOS) {
            registrations = registrations.filter(r -> !isActive(r, now));
        }

        return registrations.map(r -> r.getCategory().getEvent()).toList();
    }

    private boolean isActive(Registration registration, LocalDateTime now) {
        return registration.getStatus() == RegistrationStatus.ATIVA && registration.getCategory().getEvent().getStartDateTime().isAfter(now);
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
