package br.ifsp.demo.domain.repository;

import java.util.UUID;

public interface UserRepository {
    boolean existsById(UUID id);
}
