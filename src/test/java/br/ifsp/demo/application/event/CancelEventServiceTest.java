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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
public class CancelEventServiceTest {

    @Mock
    private UserRepository userRepository;

    private EventRepository eventRepository;

    private CancelEventServiceImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CancelEventServiceImpl(eventRepository, userRepository);
    }

    @Test
    @DisplayName("Should throw user not found exception when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> sut.execute(UUID.randomUUID(), userId));
    }
}
