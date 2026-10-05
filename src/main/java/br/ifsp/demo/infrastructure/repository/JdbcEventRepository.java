package br.ifsp.demo.infrastructure.repository;

import br.ifsp.demo.application.event.EventRepository;
import br.ifsp.demo.domain.Event;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcEventRepository implements EventRepository {

    private final JdbcTemplate jdbc;

    public JdbcEventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Event> findById(UUID id) {
        List<Event> events = jdbc.query(
                "SELECT id, name, start_date_time, end_date_time, organizer_id FROM event WHERE id = ?",
                (rs, rowNum) -> mapEvent(rs),
                id.toString()
        );

        return events.stream().findFirst().map(this::loadCategories);
    }

    @Override
    public Event save(Event event) {
        jdbc.update(
                "INSERT INTO event (id, name, start_date_time, end_date_time, organizer_id) VALUES (?, ?, ?, ?, ?)",
                event.getId().toString(),
                event.getName(),
                event.getStartDateTime().toString(),
                event.getEndDateTime().toString(),
                event.getOrganizerId().toString()
        );
        return event;
    }

    @Override
    public List<Event> findByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return jdbc.query(
                "SELECT id, name, start_date_time, end_date_time, organizer_id FROM event WHERE start_date_time >= ? AND start_date_time < ?",
                (rs, rowNum) -> mapEvent(rs),
                start.toString(),
                end.toString()
        ).stream().map(this::loadCategories).toList();
    }

    @Override
    public List<Event> findByOrganizerId(UUID organizerId) {
        return jdbc.query(
                "SELECT id, name, start_date_time, end_date_time, organizer_id FROM event WHERE organizer_id = ?",
                (rs, rowNum) -> mapEvent(rs),
                organizerId.toString()
        ).stream().map(this::loadCategories).toList();
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        return Event.restore(
                UUID.fromString(rs.getString("id")),
                rs.getString("name"),
                LocalDateTime.parse(rs.getString("start_date_time")),
                LocalDateTime.parse(rs.getString("end_date_time")),
                UUID.fromString(rs.getString("organizer_id"))
        );
    }

    private Event loadCategories(Event event) {
        jdbc.query(
                "SELECT id, name, capacity, price FROM category WHERE event_id = ?",
                rs -> {
                    event.restoreCategory(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("name"),
                            rs.getInt("capacity"),
                            rs.getDouble("price")
                    );
                },
                event.getId().toString()
        );
        return event;
    }
}
