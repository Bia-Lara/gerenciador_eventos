package br.ifsp.demo.application.event;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.exception.NullValueException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("TDD")
class CancelEventTest {

    @Test
    void testCancelEventWithNullUserThrowsNullValueException() {
        UUID organizerId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        Event event = new Event("Test Event", start, end, organizerId);

        assertThrows(NullValueException.class, () -> event.cancel(null));
    }
}