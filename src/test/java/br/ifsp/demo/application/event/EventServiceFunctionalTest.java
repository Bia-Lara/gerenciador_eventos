package br.ifsp.demo.application.event;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.dto.CreateEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
}
