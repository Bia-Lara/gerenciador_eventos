package br.ifsp.demo.dto;

import br.ifsp.demo.domain.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AvailableEventResponse(UUID id, String name, LocalDateTime startDateTime,
                                     LocalDateTime endDateTime, UUID organizerId,
                                     List<CategoryResponse> categories, long totalCapacity,
                                     long activeRegistrations) {
    public static AvailableEventResponse from(Event event, long activeRegistrations) {
        long totalCapacity = event.getCategories().stream()
                .mapToLong(category -> category.getCapacity())
                .sum();
        return new AvailableEventResponse(event.getId(), event.getName(),
                event.getStartDateTime(), event.getEndDateTime(), event.getOrganizerId(),
                event.getCategories().stream().map(CategoryResponse::from).toList(),
                totalCapacity, activeRegistrations);
    }
}
