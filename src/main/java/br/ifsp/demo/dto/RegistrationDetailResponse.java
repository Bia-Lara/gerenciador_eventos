package br.ifsp.demo.dto;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistrationDetailResponse(
        UUID registrationId,
        UUID eventId,
        String eventName,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        UUID categoryId,
        String categoryName,
        double price,
        RegistrationStatus status,
        EventStatus eventStatus
) {
    public static RegistrationDetailResponse from(Registration registration) {
        Event event = registration.getCategory().getEvent();
        return new RegistrationDetailResponse(
                registration.getId(),
                event.getId(),
                event.getName(),
                event.getStartDateTime(),
                event.getEndDateTime(),
                registration.getCategory().getId(),
                registration.getCategory().getName(),
                registration.getCategory().getPrice(),
                registration.getStatus(),
                event.getStatus()
        );
    }
}