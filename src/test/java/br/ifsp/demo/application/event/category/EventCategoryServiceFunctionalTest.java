package br.ifsp.demo.application.event.category;

import br.ifsp.demo.application.event.EventRepository;
import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.dto.CreateCategoryRequest;
import br.ifsp.demo.exception.EventAlreadyStartedException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
@Tag("Functional")
class EventCategoryServiceFunctionalTest {
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

    @ParameterizedTest(name = "capacidade {0}")
    @ValueSource(ints = {-1, 0})
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenCategoryCapacityIsZeroOrNegative")
    void shouldThrowIllegalArgumentExceptionWhenCategoryCapacityIsZeroOrNegative(int capacity) {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        var request = new CreateCategoryRequest(organizerId, event.getId(), "Pista", 10.00, capacity);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("shouldCreateCategoryWhenCapacityIsOne")
    void shouldCreateCategoryWhenCapacityIsOne() {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        var request = new CreateCategoryRequest(organizerId, event.getId(), "Pista", 10.00, 1);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category category = sut.createCategory(request);

        assertThat(category.getCapacity()).isEqualTo(1);
    }

    @Test
    @DisplayName("shouldThrowIllegalArgumentExceptionWhenCategoryPriceIsSlightlyNegative")
    void shouldThrowIllegalArgumentExceptionWhenCategoryPriceIsSlightlyNegative() {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        var request = new CreateCategoryRequest(organizerId, event.getId(), "Pista", -0.01, 100);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> sut.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category price must not be negative");
    }

    @ParameterizedTest(name = "preço {0}")
    @ValueSource(doubles = {0.0, 0.01, 10.0})
    @DisplayName("shouldCreateCategoryWhenPriceIsZeroOrPositive")
    void shouldCreateCategoryWhenPriceIsZeroOrPositive(double price) {
        UUID organizerId = UUID.randomUUID();
        Event event = new Event("Show", NOW.plusDays(1), NOW.plusDays(2), organizerId);
        var request = new CreateCategoryRequest(organizerId, event.getId(), "Pista", price, 100);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(mock(User.class)));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category category = sut.createCategory(request);

        assertThat(category.getPrice()).isEqualTo(price);
    }
}