package br.ifsp.demo.dto;

import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;

import java.util.UUID;

public record RegistrationResponse(UUID id, UUID eventId, UUID categoryId, RegistrationStatus status) {
    public static RegistrationResponse from(Registration r) {
        return new RegistrationResponse(r.getId(), r.getCategory().getEvent().getId(),
                r.getCategory().getId(), r.getStatus());
    }
}
