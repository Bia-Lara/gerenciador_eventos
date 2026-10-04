package br.ifsp.demo.services;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.repository.EventRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class EventQueryServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;

    private EventQueryService sut;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    @BeforeEach
    void setUp() {
        sut = new EventQueryService(userRepository, eventRepository);
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenOrganizerIdIsNull")
    void shouldThrowIllegalArgumentExceptionWhenOrganizerIdIsNull() {
        assertThatThrownBy(() -> sut.listByOrganizer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organizer is required");
    }

    @Test
    @DisplayName("shouldThrowUserNotFoundExceptionWhenOrganizerDoesNotExist")
    void shouldThrowUserNotFoundExceptionWhenOrganizerDoesNotExist() {
        UUID organizerId = UUID.randomUUID();
        when(userRepository.findById(organizerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.listByOrganizer(organizerId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + organizerId);
    }

    @Test
    @DisplayName("shouldReturnEmptyListWhenOrganizerHasNoEvents")
    void shouldReturnEmptyListWhenOrganizerHasNoEvents() {
        UUID organizerId = UUID.randomUUID();
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        List<Event> result = sut.listByOrganizer(organizerId);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("shouldReturnEventsCreatedByOrganizer")
    void shouldReturnEventsCreatedByOrganizer() {
        UUID organizerId = UUID.randomUUID();
        Event first = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        Event second = new Event("Palestra", NOW.plusDays(3), NOW.plusDays(4), organizerId);
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(eventRepository.findByOrganizerId(organizerId)).thenReturn(List.of(first, second));

        List<Event> result = sut.listByOrganizer(organizerId);

        assertThat(result).containsExactly(first, second);
    }
}
