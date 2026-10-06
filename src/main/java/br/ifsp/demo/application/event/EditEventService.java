package br.ifsp.demo.application.event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface EditEventService {
    void execute(UUID eventId, UUID userId, String newName, LocalDateTime newStart, LocalDateTime newEnd);
}