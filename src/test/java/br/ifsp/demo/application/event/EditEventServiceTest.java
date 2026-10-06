package br.ifsp.demo.application.event;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.exception.UserNotFoundException;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
}