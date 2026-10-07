package br.ifsp.demo.services;

import br.ifsp.demo.application.registration.RegistrationQueryService;
import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationFilter;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.infrastructure.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("Functional")
class RegistrationQueryServiceFunctionalTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RegistrationRepository registrationRepository;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    private RegistrationQueryService sut;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        sut = new RegistrationQueryService(userRepository, registrationRepository, fixedClock);
    }

    @ParameterizedTest(name = "evento começa {0}s em relação a agora")
    @CsvSource({"-1", "0"})
    @DisplayName("shouldNotListEventAsActiveWhenStartIsNowOrBefore")
    void shouldNotListEventAsActiveWhenStartIsNowOrBefore(long offsetSeconds) {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Registration registration = registration(user, NOW.plusSeconds(offsetSeconds));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(registration));

        List<Event> result = sut.listEventsByUser(userId, RegistrationFilter.ATIVOS);

        assertThat(result).isEmpty();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("shouldListEventAsActiveWhenStartIsOneSecondAfterNow")
    void shouldListEventAsActiveWhenStartIsOneSecondAfterNow() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Registration registration = registration(user, NOW.plusSeconds(1));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(registration));

        List<Event> result = sut.listEventsByUser(userId, RegistrationFilter.ATIVOS);

        assertThat(result).containsExactly(registration.getCategory().getEvent());
    }

    private Registration registration(User user, LocalDateTime eventStart) {
        Event event = new Event("Evento", eventStart, eventStart.plusDays(1), UUID.randomUUID());
        Category category = event.addCategory("Pista", 100, 50.0);
        Registration registration = new Registration(category, user);
        registration.setStatus(RegistrationStatus.ATIVA);
        return registration;
    }
}