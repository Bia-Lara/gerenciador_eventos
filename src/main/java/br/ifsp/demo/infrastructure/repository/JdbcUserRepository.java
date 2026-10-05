package br.ifsp.demo.infrastructure.repository;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.infrastructure.security.user.Role;
import br.ifsp.demo.infrastructure.security.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<User> findById(UUID id) {
        List<User> users = jdbc.query(
                "SELECT id, name, lastname, email, password, role FROM app_user WHERE id = ?",
                (rs, rowNum) -> new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                ),
                id.toString()
        );

        return users.stream().findFirst();
    }
}
