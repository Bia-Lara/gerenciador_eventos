package br.ifsp.demo.controller;

import br.ifsp.demo.application.registration.CancelRegistrationService;
import br.ifsp.demo.application.registration.RegisterToEventRequest;
import br.ifsp.demo.application.registration.RegistrationService;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.dto.RegistrationBody;
import br.ifsp.demo.dto.RegistrationResponse;
import br.ifsp.demo.infrastructure.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Registration API")
public class RegistrationController {

    private final AuthenticationInfoService authService;
    private final RegistrationService registrationService;
    private final CancelRegistrationService cancelRegistrationService;

    public RegistrationController(AuthenticationInfoService authService, RegistrationService registrationService, CancelRegistrationService cancelRegistrationService) {
        this.authService = authService;
        this.registrationService = registrationService;
        this.cancelRegistrationService = cancelRegistrationService;
    }

    @PostMapping("/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> register(@PathVariable UUID eventId, @RequestBody RegistrationBody body) {
        UUID userId = authService.getAuthenticatedUserId();
        Registration registration = registrationService.register(new RegisterToEventRequest(userId, eventId, body.categoryId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(RegistrationResponse.from(registration));
    }

    @PostMapping("/registrations/{registrationId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID registrationId) {
        UUID userId = authService.getAuthenticatedUserId();
        cancelRegistrationService.execute(registrationId, userId);
        return ResponseEntity.noContent().build();
    }
}