package br.ifsp.demo.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCategoryRequest(
        UUID organizerId,
        UUID eventId,
        String name,
        Double price,
        Integer capacity
) {
}
