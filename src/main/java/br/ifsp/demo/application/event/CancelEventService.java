package br.ifsp.demo.application.event;

import br.ifsp.demo.domain.Event;

import java.util.UUID;

public interface CancelEventService {
    Event execute(UUID eventId, UUID userId);
}
