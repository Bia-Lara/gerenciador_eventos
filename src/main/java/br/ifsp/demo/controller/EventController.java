package br.ifsp.demo.controller;

import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.application.event.EventService;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.dto.EventResponse;
import br.ifsp.demo.infrastructure.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event API")
public class EventController {

    private final AuthenticationInfoService authService;
    private final EventService eventService;

    public EventController(AuthenticationInfoService authService, EventService eventService) {
        this.authService = authService;
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@RequestBody CreateEventRequest request) {
        UUID organizerId = authService.getAuthenticatedUserId();
        Event event = eventService.createEvent(organizerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }
}

