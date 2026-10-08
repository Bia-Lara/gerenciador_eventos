package br.ifsp.demo.application.registration;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.exception.EntityNotFoundException;
import br.ifsp.demo.exception.UserNotFoundException;

import java.util.UUID;

public class CancelRegistrationServiceImpl implements CancelRegistrationService {
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public CancelRegistrationServiceImpl(RegistrationRepository registrationRepository, UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Registration execute(UUID registrationId, UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found"));

        registration.cancel(userId);
        registrationRepository.save(registration);

        return registration;
    }
}