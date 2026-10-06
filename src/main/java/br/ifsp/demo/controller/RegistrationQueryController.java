package br.ifsp.demo.controller;

import br.ifsp.demo.application.registration.RegistrationQueryService;
import br.ifsp.demo.domain.enumerations.RegistrationFilter;
import br.ifsp.demo.dto.EventResponse;
import br.ifsp.demo.infrastructure.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/registrations")
@Tag(name = "Registration Query API")
public class RegistrationQueryController {

    private final AuthenticationInfoService authService;
    private final RegistrationQueryService registrationQueryService;

    public RegistrationQueryController(AuthenticationInfoService authService, RegistrationQueryService registrationQueryService) {
        this.authService = authService;
        this.registrationQueryService = registrationQueryService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> list(@RequestParam(defaultValue = "TODOS") RegistrationFilter filter) {
        UUID userId = authService.getAuthenticatedUserId();
        return ResponseEntity.ok(registrationQueryService.listEventsByUser(userId, filter)
                .stream().map(EventResponse::from).toList());
    }
}
