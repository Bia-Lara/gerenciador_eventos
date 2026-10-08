package br.ifsp.demo.application.registration;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.exception.EntityNotFoundException;
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
public class CancelRegistrationServiceTest {
    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    private CancelRegistrationService sut;

    @BeforeEach
    void setUp() {
        sut = new CancelRegistrationServiceImpl(registrationRepository, userRepository);
    }

    @Test
    @DisplayName("Should throw entity not found exception when registration does not exist")
    void shouldThrowEntityNotFoundExceptionWhenRegistrationDoesNotExist() {
        UUID registrationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> sut.execute(registrationId, userId));
    }

    @Test
    @DisplayName("Should throw user not found exception when user does not exist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID registrationId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> sut.execute(registrationId, userId));
    }

    @Test
    @DisplayName("Should successfully cancel registration")
    void shouldSuccessfullyCancelRegistration() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);
        Event event = new Event("Test Event", start, end, UUID.randomUUID());
        Category category = event.addCategory("Test Category", 10, 50.0);
        User user = new User();
        Registration registration = new Registration(category, user);
        UUID userId = user.getId();
        UUID registrationId = registration.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));

        Registration result = sut.execute(registrationId, userId);

        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.CANCELADA);
        verify(registrationRepository).save(result);
    }
}
