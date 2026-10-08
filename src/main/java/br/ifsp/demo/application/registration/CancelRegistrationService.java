package br.ifsp.demo.application.registration;

import java.util.UUID;

public interface CancelRegistrationService {
    void execute(UUID registrationId, UUID userId);
}
