package br.ifsp.demo.infrastructure.security.user;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FakeUserStore {

    public static final String FAKE_PASSWORD = "123456";

    private final List<User> users = List.of(
            new User(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Organizador",
                    "Fake",
                    "organizador@fake.local",
                    FAKE_PASSWORD,
                    Role.USER
            ),
            new User(
                    UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    "Participante",
                    "Fake",
                    "participante@fake.local",
                    FAKE_PASSWORD,
                    Role.USER
            ),
            new User(
                    UUID.fromString("00000000-0000-0000-0000-000000000003"),
                    "Outro Organizador",
                    "Fake",
                    "outro-organizador@fake.local",
                    FAKE_PASSWORD,
                    Role.USER
            ),
            new User(
                    UUID.fromString("00000000-0000-0000-0000-000000000004"),
                    "Admin",
                    "Fake",
                    "admin@fake.local",
                    FAKE_PASSWORD,
                    Role.ADMIN
            )
    );

    public Optional<User> findById(UUID id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}
