package br.ifsp.demo.application.registration;

import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.exception.EntityNotFoundException;

import java.util.UUID;

public class CancelRegistrationServiceImpl implements CancelRegistrationService {
    private final RegistrationRepository registrationRepository;

    public CancelRegistrationServiceImpl(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Override
    public void execute(UUID registrationId, UUID userId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found"));

        registration.cancel(userId);
        registrationRepository.save(registration);
    }
}