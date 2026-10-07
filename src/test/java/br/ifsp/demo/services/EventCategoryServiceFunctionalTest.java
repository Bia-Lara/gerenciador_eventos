package br.ifsp.demo.services;

import br.ifsp.demo.application.event.EventRepository;
import br.ifsp.demo.application.event.category.CategoryRepository;
import br.ifsp.demo.application.event.category.EventCategoryService;
import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.exception.EventAlreadyStartedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("Functional")
class EventCategoryServiceFunctionalTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    private EventCategoryService sut;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        sut = new EventCategoryService(userRepository, eventRepository, registrationRepository, categoryRepository, fixedClock);
    }

    @ParameterizedTest(name = "evento começa {0}s em relação a agora")
    @ValueSource(longs = {-1, 0})
    @DisplayName("shouldThrowEventAlreadyStartedExceptionWhenStartIsNowOrBefore")
    void shouldThrowEventAlreadyStartedExceptionWhenStartIsNowOrBefore(long offsetSeconds) {
        LocalDateTime start = NOW.plusSeconds(offsetSeconds);
        Event event = new Event("Show", start, start.plusHours(3), UUID.randomUUID());
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> sut.deleteCategory(UUID.randomUUID(), event.getId(), UUID.randomUUID()))
                .isInstanceOf(EventAlreadyStartedException.class)
                .hasMessage("Event has already started");
    }
}