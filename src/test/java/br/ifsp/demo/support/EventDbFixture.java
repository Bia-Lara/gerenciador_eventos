package br.ifsp.demo.support;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class EventDbFixture {

    private final JdbcTemplate jdbc;

    public EventDbFixture(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UUID insertStartedEvent(UUID organizerId) {
        UUID eventId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS);
        jdbc.update(
                "INSERT INTO event (id, name, start_date_time, end_date_time, organizer_id) VALUES (?, ?, ?, ?, ?)",
                eventId.toString(),
                "Evento já iniciado",
                start.toString(),
                start.plusHours(3).toString(),
                organizerId.toString()
        );
        return eventId;
    }
}
