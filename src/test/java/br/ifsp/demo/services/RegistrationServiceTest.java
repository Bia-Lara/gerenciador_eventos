package br.ifsp.demo.services;

import br.ifsp.demo.domain.repository.EventRepository;
import br.ifsp.demo.domain.repository.RegistrationRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import br.ifsp.demo.dto.RegisterToEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class RegistrationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RegistrationRepository registrationRepository;

    private RegistrationService sut;

    @BeforeEach
    void setUp() {
        sut = new RegistrationService(userRepository, eventRepository, registrationRepository, Clock.systemDefaultZone());
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenEventIsNull")
    void shouldThrowIllegalArgumentExceptionWhenEventIsNull() {
        var request = new RegisterToEventRequest(UUID.randomUUID(), null, UUID.randomUUID());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event is required");
    }
}
