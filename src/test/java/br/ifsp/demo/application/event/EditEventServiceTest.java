package br.ifsp.demo.application.event;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.exception.EventNotFoundException;
import br.ifsp.demo.exception.UserNotFoundException;
import br.ifsp.demo.infrastructure.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class EditEventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    private EditEventServiceImpl sut;
    private UUID organizerId;
    private UUID otherUserId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        sut = new EditEventServiceImpl(eventRepository, userRepository);
        organizerId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID eventId = UUID.randomUUID();
        when(userRepository.findById(otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(eventId, otherUserId, "Test Event", startTime, endTime))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when event does not exist")
    void shouldThrowEventNotFoundExceptionWhenEventDoesNotExist() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());
        when(userRepository.findById(otherUserId)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> sut.execute(eventId, otherUserId, "New Name", startTime, endTime))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("Should successfully edit event")
    void shouldSuccessfullyEditEvent() {
        Event event = new Event("Test Event", startTime, endTime, organizerId);
        UUID eventId = event.getId();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(new User()));

        String newName = "Updated Event";
        LocalDateTime newStart = startTime.plusHours(1);
        LocalDateTime newEnd = endTime.plusHours(1);

        Event result = sut.execute(eventId, organizerId, newName, newStart, newEnd);

        verify(eventRepository).save(event);
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getStartDateTime()).isEqualTo(newStart);
        assertThat(result.getEndDateTime()).isEqualTo(newEnd);
    }
}