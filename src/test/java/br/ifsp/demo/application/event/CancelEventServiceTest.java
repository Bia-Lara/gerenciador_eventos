package br.ifsp.demo.application.event;

import br.ifsp.demo.application.event.category.EventCategoryService;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@Tag("TDD")
public class CancelEventServiceTest {

    @Mock
    private UserRepository userRepository;

    private EventRepository eventRepository;

    private CancelEventService sut;

    @BeforeEach
    void setUp() {
        sut = new CancelEventService(eventRepository, userRepository);
    }

    @Test
    @DisplayName("Should throw user not found exception when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findById(UUID.randomUUID())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.execute(eventId, any()));
    }
}
