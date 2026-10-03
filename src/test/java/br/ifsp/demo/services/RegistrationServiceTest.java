package br.ifsp.demo.services;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.repository.EventRepository;
import br.ifsp.demo.domain.repository.RegistrationRepository;
import br.ifsp.demo.domain.repository.UserRepository;
import br.ifsp.demo.dto.RegisterToEventRequest;
import br.ifsp.demo.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
        when(userRepository.existsById(userId)).thenReturn(false);
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
        when(userRepository.existsById(userId)).thenReturn(true);
        var request = new RegisterToEventRequest(userId, event.getId(), categoryId);

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found: " + categoryId);
    }

    @Test
    @DisplayName("shouldThrowEventAlreadyStartedExceptionWhenEventHasStarted")
    void shouldThrowEventAlreadyStartedExceptionWhenEventHasStarted() {
        Event started = new Event("Show", NOW.minusHours(1), NOW.plusHours(2), UUID.randomUUID());
        when(eventRepository.findById(started.getId())).thenReturn(Optional.of(started));
        var request = new RegisterToEventRequest(UUID.randomUUID(), started.getId(), UUID.randomUUID());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(EventAlreadyStartedException.class)
                .hasMessage("Event has already started");
    }

    @Test
    @DisplayName("shouldThrowDuplicateRegistrationExceptionWhenUserAlreadyHasActiveRegistration")
    void shouldThrowDuplicateRegistrationExceptionWhenUserAlreadyHasActiveRegistration() {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        Category category = event.addCategory("Pista", 100);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(registrationRepository.existsActiveByUserIdAndEventId(userId, event.getId())).thenReturn(true);
        var request = new RegisterToEventRequest(userId, event.getId(), category.getId());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(DuplicateRegistrationException.class)
                .hasMessage("User already registered in this event");
    }

    @ParameterizedTest
    @ValueSource(longs = {2, 3})
    @DisplayName("shouldThrowCategoryFullExceptionWhenNoVacanciesLeft")
    void shouldThrowCategoryFullExceptionWhenNoVacanciesLeft(long activeRegistrations) {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        Category category = event.addCategory("Pista", 2);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(registrationRepository.countActiveByCategoryId(category.getId())).thenReturn(activeRegistrations);
        var request = new RegisterToEventRequest(userId, event.getId(), category.getId());

        assertThatThrownBy(() -> sut.register(request))
                .isInstanceOf(CategoryFullException.class)
                .hasMessage("No vacancies left for this category");
    }
}
