package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.enumerations.RegistrationFilter;
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
}