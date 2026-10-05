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
import br.ifsp.demo.dto.CreateCategoryRequest;
import br.ifsp.demo.exception.ActionNotAllowedException;
import br.ifsp.demo.exception.EventAlreadyStartedException;
import br.ifsp.demo.exception.EventNotFoundException;
import br.ifsp.demo.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
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



    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenEventIsNullOnCategoryCreation")
    void shouldThrowIllegalArgumentExceptionWhenEventIsNullOnCategoryCreation() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                UUID.randomUUID(),
                null,
                "Pista",
                10.00,
                100
        );

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event is required");
    }

    @Test
    @DisplayName("shouldThrowEventNotFoundExceptionWhenEventDoesNotExistOnCategoryCreation")
    void shouldThrowEventNotFoundExceptionWhenEventDoesNotExistOnCategoryCreation() {
        UUID eventId = UUID.randomUUID();
        CreateCategoryRequest request = new CreateCategoryRequest(
                UUID.randomUUID(),
                eventId,
                "Pista",
                10.00,
                100
        );
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event not found: " + eventId);
    }


    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenOrganizerIsNullOnCategoryCreation")
    void shouldThrowIllegalArgumentExceptionWhenOrganizerIsNullOnCategoryCreation() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                null,
                UUID.randomUUID(),
                "Pista",
                10.00,
                100
        );

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Organizer is required");
    }


    @Test
    @DisplayName("shouldThrowUserNotFoundExceptionWhenOrganizerDoesNotExistOnCategoryCreation")
    void shouldThrowUserNotFoundExceptionWhenOrganizerDoesNotExistOnCategoryCreation() {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                "Pista",
                10.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + organizerId);
    }


    @Test
    @DisplayName("shouldThrowActionNotAllowedExceptionWhenOrganizerIsNotTheEventCreatorOnCategoryCreation")
    void shouldThrowActionNotAllowedExceptionWhenOrganizerIsNotTheEventCreatorOnCategoryCreation() {
        UUID eventOrganizerId = UUID.randomUUID();
        UUID requestOrganizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), eventOrganizerId);
        CreateCategoryRequest request = new CreateCategoryRequest(
                requestOrganizerId,
                event.getId(),
                "Pista",
                10.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(requestOrganizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(ActionNotAllowedException.class)
                .hasMessage("Only the event organizer can create categories");
    }


    @ParameterizedTest()
    @ValueSource(longs = {0})
    @DisplayName("shouldThrowEventAlreadyStartedExceptionWhenStartIsNowOrBeforeOnCategoryCreation")
    void shouldThrowEventAlreadyStartedExceptionWhenStartIsNowOrBeforeOnCategoryCreation(long offsetSeconds) {
        UUID organizerId = UUID.randomUUID();
        LocalDateTime start = NOW.plusSeconds(offsetSeconds);
        Event event = new Event("Show", start, start.plusHours(3), organizerId);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                "Pista",
                10.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(EventAlreadyStartedException.class)
                .hasMessage("Event has already started");
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenCategoryNameIsBlankOrNull")
    void shouldThrowIllegalArgumentExceptionWhenCategoryNameIsBlankOrNull(String name) {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                name,
                10.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name is required");
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenCategoryNameExceedsMaximumLength")
    void shouldThrowIllegalArgumentExceptionWhenCategoryNameExceedsMaximumLength() {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        String name = "a".repeat(151);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                name,
                10.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name must not exceed 150 characters");
    }

    @Test
    @DisplayName("shouldCreateCategoryWhenNameHasMaximumLength")
    void shouldCreateCategoryWhenNameHasMaximumLength() {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        String name = "a".repeat(150);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                name,
                10.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category category = sut.createCategory(request);

        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo(name);
    }

    @ParameterizedTest
    @MethodSource("invalidCategoryPrices")
    @DisplayName("shouldValidateCategoryPrice")
    void shouldValidateCategoryPrice(Double price, String expectedMessage) {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                "Pista",
                price,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    private static Object[][] invalidCategoryPrices() {
        return new Object[][]{
                {-1.00, "Category price must not be negative"},
                {null, "Category price is required"}
        };
    }

    @Test
    @DisplayName("shouldCreateCategoryWhenPriceIsZero")
    void shouldCreateCategoryWhenPriceIsZero() {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                "Pista",
                0.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category category = sut.createCategory(request);

        assertThat(category.getPrice()).isEqualTo(0.00);
    }

    @ParameterizedTest
    @MethodSource("invalidCategoryCapacities")
    @DisplayName("shouldValidateCategoryCapacity")
    void shouldValidateCategoryCapacity(Integer capacity, String expectedMessage) {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                "Pista",
                10.00,
                capacity
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    private static Object[][] invalidCategoryCapacities() {
        return new Object[][]{
                {-1, "Category capacity must not be negative"},
                {null, "Category capacity is required"}
        };
    }

    @Test
    @DisplayName("shouldThrowEntityAlreadyExistsExceptionWhenCategoryNameAlreadyExistsOnEvent")
    void shouldThrowEntityAlreadyExistsExceptionWhenCategoryNameAlreadyExistsOnEvent() {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        event.addCategory("Pista", 100, 10.00);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                "Pista",
                10.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessage("Category name already exists");
    }

    @Test
    @DisplayName("shouldCreateCategoryWhenEventOrganizerNamePriceAndCapacityAreValid")
    void shouldCreateCategoryWhenEventOrganizerNamePriceAndCapacityAreValid() {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        CreateCategoryRequest request = new CreateCategoryRequest(
                organizerId,
                event.getId(),
                "Pista",
                10.00,
                100
        );

        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category category = sut.createCategory(request);

        assertThat(category).isNotNull();
        assertThat(category.getEvent()).isEqualTo(event);
        assertThat(category.getName()).isEqualTo("Pista");
        assertThat(category.getPrice()).isEqualTo(10.00);
        assertThat(category.getCapacity()).isEqualTo(100);
        verify(categoryRepository).save(category);
    }
}
