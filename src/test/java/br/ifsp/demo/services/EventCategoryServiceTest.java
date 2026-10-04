package br.ifsp.demo.services;

import br.ifsp.demo.application.event.category.EventCategoryService;
import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.application.event.category.CategoryRepository;
import br.ifsp.demo.application.event.EventRepository;
import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.exception.*;
import br.ifsp.demo.infrastructure.security.user.User;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("TDD")
class EventCategoryServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private RegistrationRepository registrationRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    private EventCategoryService sut;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        sut = new EventCategoryService(userRepository, eventRepository, registrationRepository, categoryRepository, fixedClock);
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenEventIsNull")
    void shouldThrowIllegalArgumentExceptionWhenEventIsNull() {
        assertThatThrownBy(() -> sut.deleteCategory(UUID.randomUUID(), null, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event is required");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenCategoryIsNull")
    void shouldThrowIllegalArgumentExceptionWhenCategoryIsNull() {
        assertThatThrownBy(() -> sut.deleteCategory(UUID.randomUUID(), UUID.randomUUID(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category is required");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenUserIsNull")
    void shouldThrowIllegalArgumentExceptionWhenUserIsNull() {
        assertThatThrownBy(() -> sut.deleteCategory(null, UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is required");
    }

    @Test
    @DisplayName("shouldThrowEventNotFoundExceptionWhenEventDoesNotExist")
    void shouldThrowEventNotFoundExceptionWhenEventDoesNotExist() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.deleteCategory(UUID.randomUUID(), eventId, UUID.randomUUID()))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event not found: " + eventId);
    }

    @ParameterizedTest(name = "evento começa {0}s em relação a agora")
    @ValueSource(longs = {-1, 0})
    @DisplayName("shouldThrowEventAlreadyStartedExceptionWhenStartIsNowOrBefore")
    void shouldThrowEventAlreadyStartedExceptionWhenStartIsNowOrBefore(long offsetSeconds) {
        LocalDateTime start = NOW.plusSeconds(offsetSeconds);
        Event event = new Event("Show", start, start.plusHours(3), UUID.randomUUID());
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> sut.deleteCategory(UUID.randomUUID(), event.getId(), UUID.randomUUID()))
                .isInstanceOf(EventAlreadyStartedException.class)
                .hasMessage("Event has already started");
    }

    @Test
    @DisplayName("shouldThrowUserNotFoundExceptionWhenUserDoesNotExist")
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.deleteCategory(userId, event.getId(), UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + userId);
    }

    @Test
    @DisplayName("shouldThrowActionNotAllowedExceptionWhenUserIsNotTheEventOrganizer")
    void shouldThrowActionNotAllowedExceptionWhenUserIsNotTheEventOrganizer() {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), UUID.randomUUID());
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.deleteCategory(userId, event.getId(), UUID.randomUUID()))
                .isInstanceOf(ActionNotAllowedException.class)
                .hasMessage("Only the event organizer can delete categories");
    }

    @Test
    @DisplayName("shouldThrowCategoryNotFoundExceptionWhenEventDoesNotHaveTheCategory")
    void shouldThrowCategoryNotFoundExceptionWhenEventDoesNotHaveTheCategory() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), userId);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.deleteCategory(userId, event.getId(), categoryId))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category not found: " + categoryId);
    }

    @Test
    @DisplayName("shouldThrowActionNotAllowedExceptionWhenCategoryHasRegistrations")
    void shouldThrowActionNotAllowedExceptionWhenCategoryHasRegistrations() {
        UUID userId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), userId);
        Category category = event.addCategory("Pista", 50, 50.0);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(registrationRepository.existsByCategoryId(category.getId())).thenReturn(true);

        assertThatThrownBy(() -> sut.deleteCategory(userId, event.getId(), category.getId()))
                .isInstanceOf(ActionNotAllowedException.class)
                .hasMessage("Category has registrations");
    }

    @Test
    @DisplayName("shouldDeleteCategoryWhenOrganizerOwnsEventAndCategoryHasNoRegistrations")
    void shouldDeleteCategoryWhenOrganizerOwnsEventAndCategoryHasNoRegistrations() {
        UUID userId = UUID.randomUUID();
        LocalDateTime start = NOW.plusSeconds(1);
        Event event = new Event("Show", start, start.plusHours(3), userId);
        Category category = event.addCategory("Pista", 50, 50.0);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(registrationRepository.existsByCategoryId(category.getId())).thenReturn(false);

        sut.deleteCategory(userId, event.getId(), category.getId());

        verify(categoryRepository).delete(category);
        assertThat(event.findCategory(category.getId())).isEmpty();
    }
}