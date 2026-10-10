package br.ifsp.demo.infrastructure.repository;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.exception.UserNotFoundException;
import br.ifsp.demo.infrastructure.security.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcRegistrationRepository implements RegistrationRepository {

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;

    public JdbcRegistrationRepository(JdbcTemplate jdbc, UserRepository userRepository) {
        this.jdbc = jdbc;
        this.userRepository = userRepository;
    }

    @Override
    public boolean existsActiveByEventId(UUID eventId) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM registration r
                JOIN category c ON c.id = r.category_id
                WHERE c.event_id = ?
                  AND r.status = ?
                """,
                Integer.class,
                eventId.toString(),
                RegistrationStatus.ATIVA.name()
        );

        return count != null && count > 0;
    }

    @Override
    public boolean existsActiveByUserIdAndEventId(UUID userId, UUID eventId) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM registration r
                JOIN category c ON c.id = r.category_id
                WHERE r.user_id = ?
                  AND c.event_id = ?
                  AND r.status = ?
                """,
                Integer.class,
                userId.toString(),
                eventId.toString(),
                RegistrationStatus.ATIVA.name()
        );

        return count != null && count > 0;
    }

    @Override
    public long countActiveByCategoryId(UUID categoryId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM registration WHERE category_id = ? AND status = ?",
                Long.class,
                categoryId.toString(),
                RegistrationStatus.ATIVA.name()
        );

        return count == null ? 0 : count;
    }

    @Override
    public Registration create(Registration registration) {
        jdbc.update(
                "INSERT INTO registration (id, user_id, category_id, status) VALUES (?, ?, ?, ?)",
                registration.getId().toString(),
                registration.getUser().getId().toString(),
                registration.getCategory().getId().toString(),
                registration.getStatus().name()
        );
        return registration;
    }

    @Override
    public Registration save(Registration registration) {
        jdbc.update(
                "UPDATE registration SET user_id = ?, category_id = ?, status = ? WHERE id = ?",
                registration.getUser().getId().toString(),
                registration.getCategory().getId().toString(),
                registration.getStatus().name(),
                registration.getId().toString()
        );
        return registration;
    }

    @Override
    public List<Registration> findByUserId(UUID userId) {
        return jdbc.query(
                """
                SELECT
                    r.id AS registration_id,
                    r.user_id,
                    r.status,
                    c.id AS category_id,
                    c.name AS category_name,
                    c.capacity AS category_capacity,
                    c.price AS category_price,
                    e.id AS event_id,
                    e.name AS event_name,
                    e.start_date_time,
                    e.end_date_time,
                    e.organizer_id
                FROM registration r
                JOIN category c ON c.id = r.category_id
                JOIN event e ON e.id = c.event_id
                WHERE r.user_id = ?
                """,
                (rs, rowNum) -> mapRegistration(rs),
                userId.toString()
        );
    }

    @Override
    public boolean existsByCategoryId(UUID categoryId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM registration WHERE category_id = ?",
                Integer.class,
                categoryId.toString()
        );

        return count != null && count > 0;
    }

    @Override
    public Optional<Registration> findById(UUID registrationId) {
        List<Registration> registrations = jdbc.query(
                """
                SELECT
                    r.id AS registration_id,
                    r.user_id,
                    r.status,
                    c.id AS category_id,
                    c.name AS category_name,
                    c.capacity AS category_capacity,
                    c.price AS category_price,
                    e.id AS event_id,
                    e.name AS event_name,
                    e.start_date_time,
                    e.end_date_time,
                    e.organizer_id
                FROM registration r
                JOIN category c ON c.id = r.category_id
                JOIN event e ON e.id = c.event_id
                WHERE r.id = ?
                """,
                (rs, rowNum) -> mapRegistration(rs),
                registrationId.toString()
        );

        return registrations.stream().findFirst();
    }

    private Registration mapRegistration(ResultSet rs) throws SQLException {
        UUID registrationId = UUID.fromString(rs.getString("registration_id"));
        UUID userId = UUID.fromString(rs.getString("user_id"));
        UUID categoryId = UUID.fromString(rs.getString("category_id"));

        RegistrationStatus status =
                RegistrationStatus.valueOf(rs.getString("status"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UUID eventId = UUID.fromString(rs.getString("event_id"));
        String eventName = rs.getString("event_name");

        LocalDateTime startDateTime =
                LocalDateTime.parse(rs.getString("start_date_time"));

        LocalDateTime endDateTime =
                LocalDateTime.parse(rs.getString("end_date_time"));

        UUID organizerId = UUID.fromString(rs.getString("organizer_id"));

        Event event = Event.restore(
                eventId,
                eventName,
                startDateTime,
                endDateTime,
                organizerId
        );

        Category category = event.restoreCategory(
                categoryId,
                rs.getString("category_name"),
                rs.getInt("category_capacity"),
                rs.getDouble("category_price")
        );

        return Registration.restore(
                registrationId,
                category,
                user,
                status
        );
    }
}
