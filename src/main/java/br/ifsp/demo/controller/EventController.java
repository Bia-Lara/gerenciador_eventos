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
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.application.event.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event API")
public class EventController {

    private final AuthenticationInfoService authService;
    private final EventQueryService eventQueryService;
    private final EditEventService editEventService;
    private final CancelEventService cancelEventService;
    private final EventService eventService;

    public EventController(AuthenticationInfoService authService, EventQueryService eventQueryService, EditEventService editEventService, CancelEventService cancelEventService, EventService eventService) {
        this.authService = authService;
        this.eventQueryService = eventQueryService;
        this.editEventService = editEventService;
        this.cancelEventService = cancelEventService;
        this.eventService = eventService;
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
    public ResponseEntity<EventResponse> cancel(@PathVariable UUID eventId) {
        UUID userId = authService.getAuthenticatedUserId();
        Event cancelledEvent = cancelEventService.execute(eventId, userId);
        return ResponseEntity.ok(EventResponse.from(cancelledEvent));
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@RequestBody CreateEventRequest request) {
        UUID organizerId = authService.getAuthenticatedUserId();
        Event event = eventService.createEvent(organizerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }
}