package br.ifsp.demo.application.registration;

import br.ifsp.demo.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class CancelRegistrationServiceTest {
    @Mock
    private RegistrationRepository registrationRepository;

    private CancelRegistrationService sut;

    @BeforeEach
    void setUp() {
        sut = new CancelRegistrationService(registrationRepository);
    }

    @Test
    @DisplayName("Should throw entity not found exception when registration does not exist")
    void shouldThrowEntityNotFoundExceptionWhenRegistrationDoesNotExist() {
        UUID registrationId = UUID.randomUUID();

        when(registrationRepository.findById(registrationId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> sut.execute(registrationId));
    }
}
