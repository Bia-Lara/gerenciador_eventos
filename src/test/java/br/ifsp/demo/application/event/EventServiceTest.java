package br.ifsp.demo.application.event;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.exception.EntityNotFoundException;
import br.ifsp.demo.infrastructure.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class EventServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    private EventService sut;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        sut = new EventService(userRepository, eventRepository, fixedClock);
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenOrganizerIsNull")
    void shouldThrowIllegalArgumentExceptionWhenOrganizerIsNull() {
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThatThrownBy(() -> sut.createEvent(null, request))
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
                LocalDateTime.now().plusDays(2)
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.createEvent(organizerId, request))
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
                LocalDateTime.now().plusDays(2)
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createEvent(organizerId, request))
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
                LocalDateTime.now().plusDays(2)
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createEvent(organizerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event name must not exceed 150 characters");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenStartDateTimeIsNull")
    void shouldThrowIllegalArgumentExceptionWhenStartDateTimeIsNull() {
        UUID organizerId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                null,
                LocalDateTime.now().plusDays(2)
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createEvent(organizerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date time is required");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenEndDateTimeIsNull")
    void shouldThrowIllegalArgumentExceptionWhenEndDateTimeIsNull() {
        UUID organizerId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                LocalDateTime.now().plusDays(1),
                null
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createEvent(organizerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date time is required");
    }

    @Test
    @DisplayName("shouldCreateEventWhenAllDataIsValid")
    void shouldCreateEventWhenAllDataIsValid() {
        UUID organizerId = UUID.randomUUID();
        LocalDateTime startDateTime = NOW.plusDays(1);
        LocalDateTime endDateTime = NOW.plusDays(2);
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                startDateTime,
                endDateTime
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event event = sut.createEvent(organizerId, request);

        assertThat(event.getName()).isEqualTo("Novo evento");
        assertThat(event.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(event.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(event.getOrganizerId()).isEqualTo(organizerId);
    }
}
