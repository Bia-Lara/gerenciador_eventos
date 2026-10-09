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
                NOW.plusDays(1),
                NOW.plusDays(2)
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
                NOW.plusDays(1),
                NOW.plusDays(2)
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.createEvent(organizerId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Organizer not found");
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
        when(eventRepository.create(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event event = sut.createEvent(organizerId, request);

        assertThat(event.getName()).isEqualTo("Novo evento");
        assertThat(event.getStartDateTime()).isEqualTo(startDateTime);
        assertThat(event.getEndDateTime()).isEqualTo(endDateTime);
        assertThat(event.getOrganizerId()).isEqualTo(organizerId);
    }
}
