package br.ifsp.demo.dto;

import java.time.LocalDateTime;

public record CreateEventRequest(
        String name,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {
}
