package br.ifsp.demo.infrastructure.repository;

import br.ifsp.demo.application.registration.RegistrationRepository;
import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JdbcRegistrationRepository implements RegistrationRepository {

    private final JdbcTemplate jdbc;

    public JdbcRegistrationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
    public Registration save(Registration registration) {
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
    public List<Registration> findByUserId(UUID userId) {
        throw new UnsupportedOperationException("Not implemented yet");
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
}
