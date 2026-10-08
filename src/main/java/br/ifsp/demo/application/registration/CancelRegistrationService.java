package br.ifsp.demo.application.registration;

import br.ifsp.demo.domain.Registration;

import java.util.UUID;

public interface CancelRegistrationService {
    Registration execute(UUID registrationId, UUID userId);
}
