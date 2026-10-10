package br.ifsp.demo.application.registration;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.exception.EventAlreadyStartedException;
import br.ifsp.demo.exception.NullValueException;
import br.ifsp.demo.exception.UnauthorizedUserException;
import br.ifsp.demo.infrastructure.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("TDD")
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
        user.setId(userId);
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

    @Test
    @DisplayName("Should throw event already started exception when event already started")
    void shouldThrowEventAlreadyStartedExceptionWhenEventAlreadyStarted() {
        LocalDateTime startedTime = LocalDateTime.now().minusMinutes(30);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        event = new Event("Test Event", startedTime, endTime, UUID.randomUUID());
        category = event.addCategory("Test Category", 10, 50.0);
        User user = new User();
        registration = new Registration(category, user);

        assertThrows(EventAlreadyStartedException.class, () -> registration.cancel(userId));
    }

    @Test
    @DisplayName("Should throw exception when user does not own registration")
    void shouldThrowExceptionWhenUserDoesNotOwnRegistration() {
        UUID anotherUserId = UUID.randomUUID();

        assertThrows(
                UnauthorizedUserException.class,
                () -> registration.cancel(anotherUserId)
        );
    }
}
