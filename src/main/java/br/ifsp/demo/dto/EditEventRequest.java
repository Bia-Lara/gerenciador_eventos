package br.ifsp.demo.dto;

import java.time.LocalDateTime;

public record EditEventRequest(
        String name,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {}