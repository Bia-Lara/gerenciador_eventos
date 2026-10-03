package br.ifsp.demo.dto;

import java.util.UUID;

public record RegisterToEventRequest(UUID userId, UUID eventId, UUID categoryId) {}
