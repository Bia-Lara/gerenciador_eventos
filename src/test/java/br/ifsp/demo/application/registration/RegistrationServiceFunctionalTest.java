package br.ifsp.demo.application.registration;

import br.ifsp.demo.application.event.EventRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.exception.CategoryFullException;
import br.ifsp.demo.exception.EventAlreadyStartedException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("Functional")
class RegistrationServiceFunctionalTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RegistrationRepository registrationRepository;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    private RegistrationService sut;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        sut = new RegistrationService(userRepository, eventRepository, registrationRepository, fixedClock);
    }

    @ParameterizedTest(name = "evento começa {0}s em relação a agora")
    @ValueSource(longs = {-1, 0})
    @DisplayName("shouldThrowEventAlreadyStartedExceptionWhenStartIsNowOrBefore")
    void shouldThrowEventAlreadyStartedExceptionWhenStartIsNowOrBefore(long offsetSeconds) {
        LocalDateTime start = NOW.plusSeconds(offsetSeconds);
        Event event = new Event("Show", start, start.plusHours(3), UUID.randomUUID());
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        var request = new RegisterToEventRequest(UUID.randomUUID(), event.getId(), UUID.randomUUID());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(EventAlreadyStartedException.class)
                .hasMessage("Event has already started");
    }

    @Test
    @DisplayName("shouldRegisterWhenEventStartsOneSecondAfterNow")
    void shouldRegisterWhenEventStartsOneSecondAfterNow() {
        LocalDateTime start = NOW.plusSeconds(1);
        Event event = new Event("Show", start, start.plusHours(3), UUID.randomUUID());
        Category category = event.addCategory("Pista", 50, 50.0);
        User user = mock(User.class);
        UUID userId = UUID.randomUUID();
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.create(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        Registration result = sut.register(new RegisterToEventRequest(userId, event.getId(), category.getId()));

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.ATIVA);
    }

    @ParameterizedTest(name = "capacidade {0}, ocupadas {1}")
    @CsvSource({"1, 1", "1, 2", "50, 50", "50, 51"})
    @DisplayName("shouldThrowCategoryFullExceptionWhenActiveRegistrationsReachCapacity")
    void shouldThrowCategoryFullExceptionWhenActiveRegistrationsReachCapacity(int capacity, long active) {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        Category category = event.addCategory("Pista", capacity, 50.0);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(registrationRepository.countActiveByCategoryId(category.getId())).thenReturn(active);
        var request = new RegisterToEventRequest(userId, event.getId(), category.getId());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(CategoryFullException.class)
                .hasMessage("No vacancies left for this category");
    }

    @ParameterizedTest(name = "capacidade {0}, ocupadas {1}")
    @CsvSource({"1, 0", "50, 0", "50, 49"})
    @DisplayName("shouldRegisterWhenCategoryStillHasAtLeastOneVacancy")
    void shouldRegisterWhenCategoryStillHasAtLeastOneVacancy(int capacity, long active) {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        Category category = event.addCategory("Pista", capacity, 50.0);
        User user = mock(User.class);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.countActiveByCategoryId(category.getId())).thenReturn(active);
        when(registrationRepository.create(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        Registration result = sut.register(new RegisterToEventRequest(userId, event.getId(), category.getId()));

        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.ATIVA);
        verify(registrationRepository).create(any(Registration.class));
    }
}
