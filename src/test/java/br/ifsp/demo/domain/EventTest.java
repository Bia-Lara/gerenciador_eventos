package br.ifsp.demo.domain;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.exception.EventAlreadyStartedException;
import br.ifsp.demo.exception.NullValueException;
import br.ifsp.demo.exception.UnauthorizedUserException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.time.LocalDateTime;
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
}