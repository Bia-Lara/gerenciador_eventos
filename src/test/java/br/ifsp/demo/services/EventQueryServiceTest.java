package br.ifsp.demo.services;

import br.ifsp.demo.domain.repository.EventRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import br.ifsp.demo.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class EventQueryServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;

    private EventQueryService sut;

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
}
