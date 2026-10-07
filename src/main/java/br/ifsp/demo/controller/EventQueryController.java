package br.ifsp.demo.controller;

import br.ifsp.demo.application.event.EventQueryService;
import br.ifsp.demo.dto.EventResponse;
import br.ifsp.demo.infrastructure.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Query API")
public class EventQueryController {

    private final AuthenticationInfoService authService;
    private final EventQueryService eventQueryService;

    public EventQueryController(AuthenticationInfoService authService, EventQueryService eventQueryService) {
        this.authService = authService;
        this.eventQueryService = eventQueryService;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<EventResponse>> listMine() {
        UUID userId = authService.getAuthenticatedUserId();
        return ResponseEntity.ok(eventQueryService.listByOrganizer(userId)
                .stream().map(EventResponse::from).toList());
    }
}
