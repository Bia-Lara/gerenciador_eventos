package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.domain.repository.OrganizerRepository;
import br.ifsp.demo.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class EventServiceTest {

    @Mock
    private OrganizerRepository organizerRepository;

    private EventService sut;

    @BeforeEach
    void setUp() {
        sut = new EventService(organizerRepository);
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenOrganizerIsNull")
    void shouldThrowIllegalArgumentExceptionWhenOrganizerIsNull() {
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );
        assertThatThrownBy(() -> sut.createEvent(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organizer is required");

    }

    @Test
    @DisplayName("shouldThrowEntityNotFoundExceptionWhenOrganizerDoesNotExist")
    void shouldThrowEntityNotFoundExceptionWhenOrganizerDoesNotExist() {
        UUID organizerId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                organizerId
        );

        when(organizerRepository.findById(organizerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.createEvent(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Organizer not found");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" "})
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenEventNameIsInvalid")
    void shouldThrowIllegalArgumentExceptionWhenEventNameIsInvalid(String name) {
        UUID organizerId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                name,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                organizerId
        );

        assertThatThrownBy(() -> sut.createEvent(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event name is required");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenEventNameExceedsMaximumLength")
    void shouldThrowIllegalArgumentExceptionWhenEventNameExceedsMaximumLength() {
        UUID organizerId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                "a".repeat(151),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                organizerId
        );

        assertThatThrownBy(() -> sut.createEvent(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event name must not exceed 150 characters");
    }

    @Test
    @DisplayName("shouldCreateEventWhenEventNameHasMaximumLength")
    void shouldCreateEventWhenEventNameHasMaximumLength() {
        UUID organizerId = UUID.randomUUID();
        String eventName = "a".repeat(150);
        CreateEventRequest request = new CreateEventRequest(
                eventName,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                organizerId
        );

        when(organizerRepository.findById(organizerId)).thenReturn(Optional.of(organizerId));

        Event event = sut.createEvent(request);

        assertThat(event.getName()).isEqualTo(eventName);
        assertThat(event.getName()).hasSize(150);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "'  Novo evento  ','Novo evento'",
            "' Novo evento','Novo evento'",
            "'Novo evento ','Novo evento'"
    }, ignoreLeadingAndTrailingWhitespace = false)
    @DisplayName("shouldTrimEventNameWhenCreatingEvent")
    void shouldTrimEventNameWhenCreatingEvent(String name, String expectedName) {
        UUID organizerId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                name,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                organizerId
        );

        when(organizerRepository.findById(organizerId)).thenReturn(Optional.of(organizerId));

        Event event = sut.createEvent(request);

        assertThat(event.getName()).isEqualTo(expectedName);
    }

}
