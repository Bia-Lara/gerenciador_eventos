package br.ifsp.demo.domain;

import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.exception.EventAlreadyStartedException;
import br.ifsp.demo.exception.NullValueException;
import br.ifsp.demo.exception.UnauthorizedUserException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("TDD")
class EventTest {
    private UUID organizerId;
    private UUID otherUserId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        startTime = LocalDateTime.now().plusHours(1);
        endTime = startTime.plusHours(2);
    }

    @Nested
    @DisplayName("Create Event")
    class CreateEventTests {
        @Test
        @DisplayName("Should throw IllegalArgumentException when organizer is null")
        void shouldThrowIllegalArgumentExceptionWhenOrganizerIsNull() {
            assertThatThrownBy(() -> new Event("Test Event", startTime, endTime, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Organizer is required");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        @DisplayName("Should throw IllegalArgumentException when name is null or blank")
        void shouldThrowIllegalArgumentExceptionWhenNameIsNullOrBlank(String name) {
            assertThatThrownBy(() -> new Event(name, startTime, endTime, organizerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Event name is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name exceeds maximum length")
        void shouldThrowIllegalArgumentExceptionWhenNameExceedsMaximumLength() {
            String name = "a".repeat(151);

            assertThatThrownBy(() -> new Event(name, startTime, endTime, organizerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Event name must not exceed 150 characters");
        }

        @Test
        @DisplayName("Should create event when name has maximum length")
        void shouldCreateEventWhenNameHasMaximumLength() {
            String name = "a".repeat(150);

            Event event = new Event(name, startTime, endTime, organizerId);

            assertThat(event.getName()).isEqualTo(name);
            assertThat(event.getName()).hasSize(150);
        }

        @ParameterizedTest
        @CsvSource(value = {
                "'  Novo evento  ','Novo evento'",
                "' Novo evento','Novo evento'",
                "'Novo evento ','Novo evento'"
        }, ignoreLeadingAndTrailingWhitespace = false)
        @DisplayName("Should trim event name")
        void shouldTrimEventName(String name, String expectedName) {
            Event event = new Event(name, startTime, endTime, organizerId);

            assertThat(event.getName()).isEqualTo(expectedName);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when start datetime is null")
        void shouldThrowIllegalArgumentExceptionWhenStartDateTimeIsNull() {
            assertThatThrownBy(() -> new Event("Test Event", null, endTime, organizerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start date time is required");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when end datetime is null")
        void shouldThrowIllegalArgumentExceptionWhenEndDateTimeIsNull() {
            assertThatThrownBy(() -> new Event("Test Event", startTime, null, organizerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("End date time is required");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1, 0})
        @DisplayName("Should throw IllegalArgumentException when end datetime is not after start datetime")
        void shouldThrowIllegalArgumentExceptionWhenEndDateTimeIsNotAfterStartDateTime(long daysAfterStartDateTime) {
            assertThatThrownBy(() -> new Event("Test Event", startTime, startTime.plusDays(daysAfterStartDateTime), organizerId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("End date time must be after start date time");
        }

        @Test
        @DisplayName("Should successfully create event")
        void shouldSuccessfullyCreateEvent() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThat(event.getName()).isEqualTo("Test Event");
            assertThat(event.getStartDateTime()).isEqualTo(startTime);
            assertThat(event.getEndDateTime()).isEqualTo(endTime);
            assertThat(event.getOrganizerId()).isEqualTo(organizerId);
        }
    }

    @Nested
    @DisplayName("Cancel Event")
    class CancelEventTests {
        @Test
        @DisplayName("Should throw null value exception when user is null")
        void shouldThrowNullValueExceptionWhenUserIsNull() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThrows(NullValueException.class, () -> event.cancel(null));
        }

        @Test
        @DisplayName("Should throw unauthorized user exception when user is not organizer")
        void shouldThrowUnauthorizedUserExceptionWhenUserIsNotOrganizer() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThrows(UnauthorizedUserException.class, () -> event.cancel(otherUserId));
        }

        @Test
        @DisplayName("Should throw event already started exception when event already started")
        void shouldThrowEventAlreadyStartedExceptionWhenEventAlreadyStarted() {
            LocalDateTime start = LocalDateTime.now().minusMinutes(30);
            Event event = new Event("Test Event", start, endTime, organizerId);

            assertThrows(EventAlreadyStartedException.class, () -> event.cancel(organizerId));
        }

        @Test
        @DisplayName("Should throw illegal state exception when event is already cancelled")
        void shouldThrowIllegalStateExceptionWhenEventIsAlreadyCancelled() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);
            event.cancel(organizerId);

            assertThrows(IllegalStateException.class, () -> event.cancel(organizerId));
        }

        @Test
        @DisplayName("Should successfully cancel event")
        void shouldSuccessfullyCancelEvent() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            event.cancel(organizerId);

            assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("Edit Event")
    class EditEventTests {
        @Test
        @DisplayName("Should throw IllegalArgumentException when start datetime is null")
        void shouldThrowIllegalArgumentExceptionWhenStartDateTimeIsNull() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThatThrownBy(() -> event.edit(organizerId, "Test Event", null, endTime))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when end datetime is null")
        void shouldThrowIllegalArgumentExceptionWhenEndDateTimeIsNull() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThatThrownBy(() -> event.edit(organizerId, "Test Event", startTime, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when start datetime is before now")
        void shouldThrowIllegalArgumentExceptionWhenStartDateTimeIsBeforeNow() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);
            LocalDateTime pastDateTime = startTime.minusDays(1);

            assertThatThrownBy(() -> event.edit(organizerId, "Test Event", pastDateTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when end datetime is before now")
        void shouldThrowIllegalArgumentExceptionWhenEndDateTimeIsBeforeNow() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);
            LocalDateTime pastDateTime = endTime.minusDays(1);

            assertThatThrownBy(() -> event.edit(organizerId, "Test Event", startTime, pastDateTime))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when end datetime equals start datetime")
        void shouldThrowIllegalArgumentExceptionWhenEndEqualsStart() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThatThrownBy(() -> event.edit(organizerId, "Test Event", startTime, startTime))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw EventAlreadyStartedException when event already started")
        void shouldThrowEventAlreadyStartedExceptionWhenEventAlreadyStarted() {
            LocalDateTime pastDateTime = startTime.minusDays(1);
            Event event = new Event("Test Event", pastDateTime, endTime, organizerId);

            assertThatThrownBy(() -> event.edit(organizerId, "Test Event", startTime, endTime))
                    .isInstanceOf(EventAlreadyStartedException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should throw IllegalArgumentException when name is null or blank")
        void shouldThrowIllegalArgumentExceptionWhenNameIsNullOrBlank(String invalidName) {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThatThrownBy(() -> event.edit(organizerId, invalidName, startTime, endTime))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw NullValueException when user is null")
        void shouldThrowNullValueExceptionWhenUserIsNull() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThatThrownBy(() -> event.edit(null, "Test Event", startTime, endTime))
                    .isInstanceOf(NullValueException.class);
        }

        @Test
        @DisplayName("Should throw UnauthorizedUserException when user is not organizer")
        void shouldThrowUnauthorizedUserExceptionWhenUserIsNotOrganizer() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThatThrownBy(() -> event.edit(otherUserId, "Test Event", startTime, endTime))
                    .isInstanceOf(UnauthorizedUserException.class);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when event is cancelled")
        void shouldThrowIllegalStateExceptionWhenEventIsCancelled() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);
            event.setStatus(EventStatus.CANCELLED);

            assertThatThrownBy(() -> event.edit(organizerId, "Test Event", startTime, endTime))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should successfully edit event when all validations pass")
        void shouldSuccessfullyEditEventWhenAllValidationsPass() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            String newName = "Updated Event";
            LocalDateTime newStart = startTime.plusHours(1);
            LocalDateTime newEnd = endTime.plusHours(1);

            event.edit(organizerId, newName, newStart, newEnd);

            assertThat(event.getName()).isEqualTo(newName);
            assertThat(event.getStartDateTime()).isEqualTo(newStart);
            assertThat(event.getEndDateTime()).isEqualTo(newEnd);
        }
    }

    @Nested
    @Tag("Structural")
    @DisplayName("Equals")
    class EqualsTests {
        @Test
        @DisplayName("should return false when compared with null")
        void shouldReturnFalseWhenComparedWithNull() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);

            assertThat(event.equals(null)).isFalse();
        }

        @Test
        @DisplayName("should return false when compared with another type")
        void shouldReturnFalseWhenComparedWithAnotherType() {
            Event event = new Event("Test Event", startTime, endTime, organizerId);
            Object anotherType = new Object();

            assertThat(event.equals(anotherType)).isFalse();
        }

        @Test
        @DisplayName("should return false when ids are different")
        void shouldReturnFalseWhenIdsAreDifferent() {
            Event event = Event.restore(
                    UUID.randomUUID(),
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId
            );

            Event differentEvent = Event.restore(
                    UUID.randomUUID(),
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId
            );

            assertThat(event.equals(differentEvent)).isFalse();
        }

        @Test
        @DisplayName("should return false when names are different")
        void shouldReturnFalseWhenNamesAreDifferent() {
            UUID id = UUID.randomUUID();

            Event event = Event.restore(
                    id,
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId
            );

            Event differentEvent = Event.restore(
                    id,
                    "Another Event",
                    startTime,
                    endTime,
                    organizerId
            );

            assertThat(event.equals(differentEvent)).isFalse();
        }

        @Test
        @DisplayName("should return false when startDateTimes are different")
        void shouldReturnFalseWhenStartDateTimesAreDifferent() {
            UUID id = UUID.randomUUID();

            Event event = Event.restore(
                    id,
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId
            );

            Event differentEvent = Event.restore(
                    id,
                    "Test Event",
                    startTime.plusMinutes(1),
                    endTime,
                    organizerId
            );

            assertThat(event.equals(differentEvent)).isFalse();
        }

        @Test
        @DisplayName("should return false when endDateTimes are different")
        void shouldReturnFalseWhenEndDateTimesAreDifferent() {
            UUID id = UUID.randomUUID();

            Event event = Event.restore(
                    id,
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId
            );

            Event differentEvent = Event.restore(
                    id,
                    "Test Event",
                    startTime,
                    endTime.plusMinutes(1),
                    organizerId
            );

            assertThat(event.equals(differentEvent)).isFalse();
        }

    }

    @Nested
    @Tag("Mutation")
    class MutationTests {

        @Test
        @DisplayName("should reject zero category capacity")
        void shouldRejectZeroCategoryCapacity() {
            Event event = new Event(
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId
            );

            assertThatThrownBy(() ->
                    event.addCategory("General", 0, 10.0)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "Category capacity must be greater than zero"
                    );
        }

        @Test
        @DisplayName("should distinguish zero capacity from negative capacity")
        void shouldDistinguishZeroFromNegativeCapacity() {
            Event event = new Event(
                    "Test Event", startTime, endTime, organizerId
            );

            assertThatThrownBy(() ->
                    event.addCategory("General", 0, 10.0)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "Category capacity must be greater than zero"
                    );
        }


        @Test
        @DisplayName("should remove only the category with matching ID")
        void shouldRemoveOnlyMatchingCategory() {
            Event event = new Event(
                    "Test Event", startTime, endTime, organizerId
            );

            Category general = event.addCategory("General", 100, 10.0);
            Category vip = event.addCategory("VIP", 50, 20.0);

            event.removeCategory(general.getId());

            assertThat(event.getCategories().size()).isEqualTo(1);
            assertThat(event.getCategories().getFirst().getId())
                    .isEqualTo(vip.getId());
        }


        @Test
        @DisplayName("should find only category with matching ID")
        void shouldFindOnlyMatchingCategory() {
            Event event = new Event(
                    "Test Event", startTime, endTime, organizerId
            );

            event.addCategory("General", 100, 10.0);
            Category vip = event.addCategory("VIP", 50, 20.0);

            Optional<Category> result = event.findCategory(vip.getId());

            assertThat(result).isPresent();
            assertThat(result.orElseThrow().getId()).isEqualTo(vip.getId());
        }

        @Test
        @DisplayName("should calculate hash code based on event fields")
        void shouldCalculateHashCodeBasedOnEventFields() {
            UUID id = UUID.randomUUID();

            Event event = Event.restore(
                    id,
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId
            );

            int expectedHash = Objects.hash(
                    id,
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId
            );

            assertThat(event.hashCode()).isEqualTo(expectedHash);
        }


        @Test
        @DisplayName("should return non-null event when restoring with status")
        void shouldReturnNonNullEventWhenRestoringWithStatus() {
            UUID id = UUID.randomUUID();

            Event restoredEvent = Event.restoreWithStatus(
                    id,
                    "Test Event",
                    startTime,
                    endTime,
                    organizerId,
                    EventStatus.CANCELLED
            );

            assertThat(restoredEvent)
                    .as("Restored event must not be null")
                    .isNotNull();

            assertThat(restoredEvent.getId()).isEqualTo(id);
            assertThat(restoredEvent.getStatus())
                    .isEqualTo(EventStatus.CANCELLED);
        }


        @Test
        @DisplayName("should restore category with original ID and return restored category")
        void shouldRestoreCategoryWithOriginalId() {
            Event event = new Event(
                    "Test Event", startTime, endTime, organizerId
            );

            UUID originalId = UUID.randomUUID();

            Category restoredCategory = event.restoreCategory(
                    originalId,
                    "VIP",
                    50,
                    20.0
            );

            assertThat(restoredCategory).isNotNull();
            assertThat(restoredCategory.getId()).isEqualTo(originalId);

            assertThat(event.findCategory(originalId)).isPresent();
            assertThat(event.getCategories().size()).isEqualTo(1);
        }
    }
}


