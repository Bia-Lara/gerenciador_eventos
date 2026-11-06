package br.ifsp.demo.domain;

import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.infrastructure.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("UnitTest")
public class RegistrationTest {    private UUID organizerId;
    private Category category;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        Event event = new Event("Event", start, start.plusHours(2), UUID.randomUUID());
        category = new Category(event, "name", 10, 10.0);

    }

    @Test
    @Tag("Structural")
    @DisplayName("Should restore registration")
    void shouldRestoreRegistration(){
        Registration registration = new Registration(category, mock(User.class));

        registration = Registration.restore(registration.getId(), registration.getCategory(), registration.getUser(), RegistrationStatus.CANCELADA);

        assertThat(registration).isNotNull();
        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.CANCELADA);
    }

}
