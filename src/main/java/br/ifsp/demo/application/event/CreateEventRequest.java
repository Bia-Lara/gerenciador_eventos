package br.ifsp.demo.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateEventRequest(
        String name,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        UUID organizerId
) {
}
