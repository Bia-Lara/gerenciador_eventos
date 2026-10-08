package br.ifsp.demo.application.event;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.infrastructure.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
@Tag("Functional")
class EventServiceFunctionalTest {

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
    @DisplayName("shouldCreateEventWhenEventNameHasMaximumLength")
    void shouldCreateEventWhenEventNameHasMaximumLength() {
        UUID organizerId = UUID.randomUUID();
        String eventName = "a".repeat(150);
        CreateEventRequest request = new CreateEventRequest(
                eventName,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event event = sut.createEvent(organizerId, request);

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
                LocalDateTime.now().plusDays(2)
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event event = sut.createEvent(organizerId, request);

        assertThat(event.getName()).isEqualTo(expectedName);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1})
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenStartDateTimeIsNotInFuture")
    void shouldThrowIllegalArgumentExceptionWhenStartDateTimeIsNotInFuture(long minutesBeforeNow) {
        UUID organizerId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                NOW.minusMinutes(minutesBeforeNow),
                NOW.plusDays(1)
        );

        assertThatThrownBy(() -> sut.createEvent(organizerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start date time must be in the future");
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenEndDateTimeIsNotAfterStartDateTime")
    void shouldThrowIllegalArgumentExceptionWhenEndDateTimeIsNotAfterStartDateTime(long daysAfterStartDateTime) {
        UUID organizerId = UUID.randomUUID();
        LocalDateTime startDateTime = NOW.plusDays(2);
        CreateEventRequest request = new CreateEventRequest(
                "Novo evento",
                startDateTime,
                startDateTime.plusDays(daysAfterStartDateTime)
        );

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createEvent(organizerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End date time must be after start date time");
    }
}
