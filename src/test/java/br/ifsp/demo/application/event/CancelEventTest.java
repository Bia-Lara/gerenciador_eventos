package br.ifsp.demo.application.event;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.exception.EventAlreadyStartedException;
import br.ifsp.demo.exception.NullValueException;
import br.ifsp.demo.exception.UnauthorizedUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("TDD")
class CancelEventTest {
    private UUID organizerId;
    private UUID otherUserId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    @DisplayName("Should throw null value exception when user is null")
    void shouldThrowNullValueExceptionWhenUserIsNull() {
        Event event = new Event("Test Event", startTime, endTime, organizerId);

        assertThrows(NullValueException.class, () -> event.cancel(null));
    }

    @Test
    @DisplayName("Should throw unauthorized user exception when user is not organizer")
    void shouldThrowUnauthorizedUserExceptionWhenUserIsNotOrganizer() {
        Event event = new Event("Test Event", startTime, endTime, organizerId);

        assertThrows(UnauthorizedUserException.class, () -> event.cancel(otherUserId));
    }

    @Test
    @DisplayName("Should throw event already started exception when event already started")
    void shouldThrowEventAlreadyStartedExceptionWhenEventAlreadyStarted() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(30);
        Event event = new Event("Test Event", start, endTime, organizerId);

        assertThrows(EventAlreadyStartedException.class, () -> event.cancel(organizerId));
    }

    @Test
    @DisplayName("Should throw illegal state exception when event is already cancelled")
    void shouldThrowIllegalStateExceptionWhenEventIsAlreadyCancelled() {
        Event event = new Event("Test Event", startTime, endTime, organizerId);
        event.cancel(organizerId);

        assertThrows(IllegalStateException.class, () -> event.cancel(organizerId));
    }
}