package br.ifsp.demo.services;

import br.ifsp.demo.dto.CreateEventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

class EventServiceTest {

    EventService eventService = new EventService();

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenOrganizerIsNull")
    void shouldThrowIllegalArgumentExceptionWhenOrganizerIsNull() {
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );
        assertThatThrownBy(()-> eventService.createEvent(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organizer is required");

    }

}