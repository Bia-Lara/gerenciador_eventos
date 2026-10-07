package br.ifsp.demo.application.event;

import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.enumerations.EventStatus;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
public class CancelEventServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    private CancelEventServiceImpl sut;

    private UUID organizerId;
    private UUID otherUserId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    @BeforeEach
    void setUp() {
        sut = new CancelEventServiceImpl(eventRepository, userRepository, registrationRepository);
        organizerId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Test
    @DisplayName("Should throw user not found exception when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> sut.execute(UUID.randomUUID(), userId));
    }

    @Test
    @DisplayName("Should IllegalStateException when event has active registrations")
    void shouldThrowIllegalStateExceptionWhenEventHasActiveRegistrations() {
        Event event = new Event("Test Event", startTime, endTime, organizerId);
        UUID eventId = event.getId();

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsActiveByEventId(eventId)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> sut.execute(eventId, organizerId));
    }

    @Test
    @DisplayName("Should throw event not found exception when event does not exist")
    void shouldThrowEventNotFoundExceptionWhenEventDoesNotExist() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> sut.execute(eventId, organizerId));
    }

    @Test
    @DisplayName("Should successfully cancel event when conditions are valid")
    void shouldSuccessfullyCancelEventWhenConditionsAreValid() {
        Event event = new Event("Test Event", startTime, endTime, organizerId);
        UUID eventId = event.getId();

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsActiveByEventId(eventId)).thenReturn(false);

        Event result = sut.execute(eventId, organizerId);

        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(eventRepository).save(result);
    }

    @Test
    @DisplayName("Should successfully cancel event when only cancelled registrations exist")
    void shouldSuccessfullyCancelEventWhenOnlyCancelledRegistrationsExist() {
        Event event = new Event("Test Event", startTime, endTime, organizerId);
        UUID eventId = event.getId();

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(new User()));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(registrationRepository.existsActiveByEventId(eventId)).thenReturn(false);

        sut.execute(eventId, organizerId);

        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(eventRepository).save(event);
    }
}
