package br.ifsp.demo.dto;

import br.ifsp.demo.domain.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EventResponse(UUID id, String name, LocalDateTime startDateTime,
                            LocalDateTime endDateTime, UUID organizerId,
                            List<CategoryResponse> categories) {
    public static EventResponse from(Event e) {
        return new EventResponse(e.getId(), e.getName(), e.getStartDateTime(),
                e.getEndDateTime(), e.getOrganizerId(),
                e.getCategories().stream().map(CategoryResponse::from).toList());
    }
}
