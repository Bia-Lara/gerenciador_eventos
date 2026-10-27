package br.ifsp.demo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

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
}

