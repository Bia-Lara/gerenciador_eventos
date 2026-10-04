package br.ifsp.demo.application.user;

import br.ifsp.demo.infrastructure.security.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
}
