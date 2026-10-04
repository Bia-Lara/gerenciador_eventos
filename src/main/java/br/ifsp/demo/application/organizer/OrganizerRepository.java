package br.ifsp.demo.application.organizer;

import java.util.Optional;
import java.util.UUID;

public interface OrganizerRepository {
    Optional<UUID> findById(UUID organizerId);
}
