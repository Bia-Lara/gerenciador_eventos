package br.ifsp.demo.application.registration;

import java.util.UUID;

public record RegisterToEventRequest(UUID userId, UUID eventId, UUID categoryId) {}
