package br.ifsp.demo.domain.repository;

import br.ifsp.demo.security.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
}
