package br.ifsp.demo.controller;

import br.ifsp.demo.application.event.CancelEventService;
import br.ifsp.demo.application.event.EditEventService;
import br.ifsp.demo.application.event.EventQueryService;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.dto.EditEventRequest;
import br.ifsp.demo.dto.EventResponse;
import br.ifsp.demo.infrastructure.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Query API")
public class EventController {

    private final AuthenticationInfoService authService;
    private final EventQueryService eventQueryService;
    private final EditEventService editEventService;
    private final CancelEventService cancelEventService;

    public EventController(AuthenticationInfoService authService, EventQueryService eventQueryService, EditEventService editEventService, CancelEventService cancelEventService) {
        this.authService = authService;
        this.eventQueryService = eventQueryService;
        this.editEventService = editEventService;
        this.cancelEventService = cancelEventService;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<EventResponse>> listMine() {
        UUID userId = authService.getAuthenticatedUserId();
        return ResponseEntity.ok(eventQueryService.listByOrganizer(userId)
                .stream().map(EventResponse::from).toList());
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> edit(@PathVariable UUID eventId, @RequestBody EditEventRequest request) {
        UUID userId = authService.getAuthenticatedUserId();
        Event editedEvent = editEventService.execute(eventId, userId, request.name(), request.startDateTime(), request.endDateTime());
        return ResponseEntity.ok(EventResponse.from(editedEvent));
    }

    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID eventId) {
        UUID userId = authService.getAuthenticatedUserId();
        cancelEventService.execute(eventId, userId);
        return ResponseEntity.noContent().build();
    }
}
