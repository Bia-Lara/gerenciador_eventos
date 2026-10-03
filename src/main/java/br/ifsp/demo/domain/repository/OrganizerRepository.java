package br.ifsp.demo.domain.repository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizerRepository {
    Optional<UUID> findById(UUID organizerId);
}
