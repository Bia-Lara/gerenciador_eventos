package br.ifsp.demo.application.event;

import br.ifsp.demo.domain.Event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EditEventService {
    Event execute(UUID eventId, UUID userId, String newName, LocalDateTime newStart, LocalDateTime newEnd);
}