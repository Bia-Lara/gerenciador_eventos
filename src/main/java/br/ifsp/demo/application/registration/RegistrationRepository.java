package br.ifsp.demo.application.registration;

import br.ifsp.demo.domain.Registration;

import java.util.List;
import java.util.UUID;

public interface RegistrationRepository {
    boolean existsActiveByUserIdAndEventId(UUID userId, UUID eventId);
    long countActiveByCategoryId(UUID categoryId);
    Registration save(Registration registration);
    List<Registration> findByUserId(UUID userId);
    boolean existsByCategoryId(UUID categoryId);
}