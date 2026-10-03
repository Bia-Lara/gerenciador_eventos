package br.ifsp.demo.services;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationFilter;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.domain.repository.RegistrationRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import br.ifsp.demo.exception.UserNotFoundException;
import br.ifsp.demo.security.user.User;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class RegistrationQueryServiceTest {
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

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenUserIsNull")
    void shouldThrowIllegalArgumentExceptionWhenUserIsNull() {
        assertThatThrownBy(() -> sut.listEventsByUser(null, RegistrationFilter.TODOS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is required");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenFilterIsNull")
    void shouldThrowIllegalArgumentExceptionWhenFilterIsNull() {
        assertThatThrownBy(() -> sut.listEventsByUser(UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Filter is required");
    }

    @Test
    @DisplayName("shouldThrowUserNotFoundExceptionWhenUserDoesNotExist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.listEventsByUser(userId, RegistrationFilter.TODOS))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenUserHasNoRegistrations")
    void shouldReturnEmptyListWhenUserHasNoRegistrations() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));

        List<Event> result = sut.listEventsByUser(userId, RegistrationFilter.TODOS);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldReturnAllEventsWhenFilterIsTodos")
    void shouldReturnAllEventsWhenFilterIsTodos() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Registration upcoming = registration(user, NOW.plusDays(1), RegistrationStatus.ATIVA);
        Registration started = registration(user, NOW.minusDays(1), RegistrationStatus.ATIVA);
        Registration canceled = registration(user, NOW.plusDays(2), RegistrationStatus.CANCELADA);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(upcoming, started, canceled));

        List<Event> result = sut.listEventsByUser(userId, RegistrationFilter.TODOS);

        assertThat(result).containsExactly(upcoming.getCategory().getEvent(), started.getCategory().getEvent(), canceled.getCategory().getEvent());
    }

    private Registration registration(User user, LocalDateTime eventStart, RegistrationStatus status) {
        Event event = new Event("Evento", eventStart, eventStart.plusDays(1), UUID.randomUUID());
        Category category = event.addCategory("Pista", 100);
        Registration registration = new Registration(category, user);
        registration.setStatus(status);
        return registration;
    }

    @Test
    @DisplayName("shouldReturnOnlyUpcomingNonCanceledEventsWhenFilterIsAtivos")
    void shouldReturnOnlyUpcomingNonCanceledEventsWhenFilterIsAtivos() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Registration upcoming = registration(user, NOW.plusDays(1), RegistrationStatus.ATIVA);
        Registration started = registration(user, NOW.minusDays(1), RegistrationStatus.ATIVA);
        Registration canceled = registration(user, NOW.plusDays(2), RegistrationStatus.CANCELADA);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(upcoming, started, canceled));

        List<Event> result = sut.listEventsByUser(userId, RegistrationFilter.ATIVOS);

        assertThat(result).containsExactly(upcoming.getCategory().getEvent());
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenFilterIsAtivosAndAllRegistrationsAreInactive")
    void shouldReturnEmptyListWhenFilterIsAtivosAndAllRegistrationsAreInactive() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Registration started = registration(user, NOW.minusDays(1), RegistrationStatus.ATIVA);
        Registration canceled = registration(user, NOW.plusDays(2), RegistrationStatus.CANCELADA);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(started, canceled));

        List<Event> result = sut.listEventsByUser(userId, RegistrationFilter.ATIVOS);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenFilterIsInativosAndUserHasOnlyActiveRegistrations")
    void shouldReturnEmptyListWhenFilterIsInativosAndUserHasOnlyActiveRegistrations() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Registration upcoming = registration(user, NOW.plusDays(1), RegistrationStatus.ATIVA);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(upcoming));

        List<Event> result = sut.listEventsByUser(userId, RegistrationFilter.INATIVOS);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldReturnStartedAndCanceledEventsWhenFilterIsInativos")
    void shouldReturnStartedAndCanceledEventsWhenFilterIsInativos() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Registration upcoming = registration(user, NOW.plusDays(1), RegistrationStatus.ATIVA);
        Registration started = registration(user, NOW.minusDays(1), RegistrationStatus.ATIVA);
        Registration canceled = registration(user, NOW.plusDays(2), RegistrationStatus.CANCELADA);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(upcoming, started, canceled));

        List<Event> result = sut.listEventsByUser(userId, RegistrationFilter.INATIVOS);

        assertThat(result).containsExactly(
                started.getCategory().getEvent(),
                canceled.getCategory().getEvent());
    }
}