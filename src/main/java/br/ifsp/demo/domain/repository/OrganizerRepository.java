package br.ifsp.demo.domain.repository;

import java.util.UUID;

public interface OrganizerRepository {
    boolean existsById(UUID organizerId);
}
