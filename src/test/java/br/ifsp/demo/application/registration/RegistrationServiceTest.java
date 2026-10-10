package br.ifsp.demo.application.registration;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.application.event.EventRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.exception.*;
import br.ifsp.demo.infrastructure.security.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    private RegistrationService sut;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        sut = new RegistrationService(userRepository, eventRepository, registrationRepository, fixedClock);
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenEventIsNull")
    void shouldThrowIllegalArgumentExceptionWhenEventIsNull() {
        var request = new RegisterToEventRequest(UUID.randomUUID(), null, UUID.randomUUID());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event is required");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenUserIsNull")
    void shouldThrowIllegalArgumentExceptionWhenUserIsNull() {
        var request = new RegisterToEventRequest(null, UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is required");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenCategoryIsNull")
    void shouldThrowIllegalArgumentExceptionWhenCategoryIsNull() {
        var request = new RegisterToEventRequest(UUID.randomUUID(), UUID.randomUUID(), null);

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category is required");
    }

    @Test
    @DisplayName("shouldThrowEventNotFoundExceptionWhenEventDoesNotExist")
    void shouldThrowEventNotFoundExceptionWhenEventDoesNotExist() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());
        var request = new RegisterToEventRequest(UUID.randomUUID(), eventId, UUID.randomUUID());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event not found: " + eventId);
    }

    @Test
    @DisplayName("shouldThrowUserNotFoundExceptionWhenUserDoesNotExist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        var request = new RegisterToEventRequest(userId, event.getId(), UUID.randomUUID());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
    }

    @Test
    @DisplayName("shouldThrowCategoryNotFoundExceptionWhenCategoryDoesNotExistInEvent")
    void shouldThrowCategoryNotFoundExceptionWhenCategoryDoesNotExistInEvent() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1),
                NOW.plusDays(2), UUID.randomUUID());
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        var request = new RegisterToEventRequest(userId, event.getId(), categoryId);

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found: " + categoryId);
    }

    @Test
    @DisplayName("shouldThrowDuplicateRegistrationExceptionWhenUserAlreadyHasActiveRegistration")
    void shouldThrowDuplicateRegistrationExceptionWhenUserAlreadyHasActiveRegistration() {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        Category category = event.addCategory("Pista", 100, 50.0);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.existsActiveByUserIdAndEventId(userId, event.getId())).thenReturn(true);
        var request = new RegisterToEventRequest(userId, event.getId(), category.getId());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(DuplicateRegistrationException.class)
                .hasMessage("User already registered in this event");
    }

    @Test
    @DisplayName("shouldRegisterParticipantWhenAllDataIsValidAndCategoryHasVacancies")
    void shouldRegisterParticipantWhenAllDataIsValidAndCategoryHasVacancies() {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        Category category = event.addCategory("Pista", 50, 50.0);
        User user = mock(User.class);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(registrationRepository.existsActiveByUserIdAndEventId(userId, event.getId())).thenReturn(false);
        when(registrationRepository.countActiveByCategoryId(category.getId())).thenReturn(1L);
        when(registrationRepository.create(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));
        var request = new RegisterToEventRequest(userId, event.getId(), category.getId());

        Registration result = sut.register(request);

        assertThat(result.getUser()).isSameAs(user);
        assertThat(result.getCategory()).isSameAs(category);
        assertThat(result.getCategory().getEvent().getId()).isEqualTo(event.getId());
        assertThat(result.getStatus()).isEqualTo(RegistrationStatus.ATIVA);
        verify(registrationRepository).create(any(Registration.class));
    }
}
