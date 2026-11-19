package br.ifsp.demo.support;

import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegistrationDbFixture {

    private final JdbcTemplate jdbc;

    public RegistrationDbFixture(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public UUID insertEvent(LocalDateTime start, LocalDateTime end, String status) {
        UUID eventId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO event (id, name, start_date_time, end_date_time, organizer_id, status) VALUES (?, ?, ?, ?, ?, ?)",
                eventId.toString(),
                "Evento " + eventId,
                start.toString(),
                end.toString(),
                UUID.randomUUID().toString(),
                status
        );
        return eventId;
    }
}