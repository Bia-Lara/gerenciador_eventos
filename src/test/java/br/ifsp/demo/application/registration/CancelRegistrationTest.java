package br.ifsp.demo.application.registration;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.exception.NullValueException;
import br.ifsp.demo.infrastructure.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CancelRegistrationTest {
    private UUID userId;
    private Event event;
    private Category category;
    private Registration registration;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);

        event = new Event("Test Event", start, end, organizerId);
        category = event.addCategory("Test Category", 10, 50.0);

        User user = new User();
        registration = new Registration(category, user);
    }

    @Test
    @DisplayName("Should throw null value exception when user is null")
    void shouldThrowNullValueExceptionWhenUserIsNull() {
        assertThrows(NullValueException.class, () -> registration.cancel(null));
    }

    @Test
    @DisplayName("Should successfully cancel registration")
    void shouldSuccessfullyCancelRegistration() {
        registration.cancel(userId);

        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.CANCELADA);
    }

    @Test
    @DisplayName("Should throw illegal state exception when registration already cancelled")
    void shouldThrowIllegalStateExceptionWhenRegistrationAlreadyCancelled() {
        registration.cancel(userId);
        assertThrows(IllegalStateException.class, () -> registration.cancel(userId));
    }
}
