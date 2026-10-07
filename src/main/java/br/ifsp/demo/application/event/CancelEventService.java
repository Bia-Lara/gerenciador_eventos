package br.ifsp.demo.application.event;

import java.util.UUID;

public interface CancelEventService {
    void execute(UUID eventId, UUID userId);
}
