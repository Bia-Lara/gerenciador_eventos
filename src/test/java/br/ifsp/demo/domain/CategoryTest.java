package br.ifsp.demo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Tag("Structural")
@Tag("UnitTest")
public class CategoryTest {

    @Test
    @DisplayName("Should throw when capacity is null")
    void shouldThrowWhenCapacityIsNull() {
        Event event = validEvent();

        assertThatThrownBy(() -> new Category(event, "Name", null, 10.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category capacity is required");
    }

    @Test
    @DisplayName("Should throw when capacity is negative")
    void shouldThrowWhenCapacityIsNegative() {
        Event event = validEvent();

        assertThatThrownBy(() -> new Category(event, "Name", -1, 10.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category capacity must not be negative");
    }

    private Event validEvent() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        return new Event("Event", start, start.plusHours(2), UUID.randomUUID());
    }

    @Test
    @DisplayName("Should throw when event is null")
    void shouldThrowWhenEventIsNull() {
        assertThatThrownBy(() -> new Category(
                null,
                "VIP",
                10,
                50.0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event is required");
    }

    @Test
    @Tag("Mutation")
    @DisplayName("should successfully create category with 0 capacity")
    void shouldSuccessfullyCreateCategoryWith0Capacity() {
        Event event = validEvent();
        Category category = new Category(event, "Name", 0, 10.0);

        assertThat(category.getName()).isEqualTo("Name");
        assertThat(category.getCapacity()).isEqualTo(0);
        assertThat(category.getPrice()).isEqualTo(10.0);
        assertThat(category.getEvent()).isEqualTo(event);
    }
}

