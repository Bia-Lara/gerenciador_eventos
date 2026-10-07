package br.ifsp.demo.controller;

import br.ifsp.demo.application.event.category.EventCategoryService;
import br.ifsp.demo.infrastructure.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Category API")
public class EventCategoryController {

    private final AuthenticationInfoService authService;
    private final EventCategoryService eventCategoryService;

    public EventCategoryController(AuthenticationInfoService authService, EventCategoryService eventCategoryService) {
        this.authService = authService;
        this.eventCategoryService = eventCategoryService;
    }

    @DeleteMapping("/{eventId}/categories/{categoryId}")
    public ResponseEntity<Void> delete(@PathVariable UUID eventId, @PathVariable UUID categoryId) {
        UUID userId = authService.getAuthenticatedUserId();
        eventCategoryService.deleteCategory(userId, eventId, categoryId);
        return ResponseEntity.noContent().build();
    }
}