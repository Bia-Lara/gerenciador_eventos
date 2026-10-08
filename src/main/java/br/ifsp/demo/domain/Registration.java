package br.ifsp.demo.domain;

import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.exception.NullValueException;
import br.ifsp.demo.infrastructure.security.user.User;

import java.util.UUID;

public class Registration {
    private UUID id;
    private final User user;
    private final Category category;
    private RegistrationStatus status;

    public Registration(Category category, User user) {
        this.id = UUID.randomUUID();
        this.category = category;
        this.user = user;
        status = RegistrationStatus.ATIVA;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public static Registration restore(UUID id, Category category, User user, RegistrationStatus status) {
        Registration registration = new Registration(category, user);
        registration.id = id;
        registration.setStatus(status);
        return registration;
    }

    public void cancel(UUID userId) {
        if (userId == null) {
            throw new NullValueException("User");
        }

        if (this.status == RegistrationStatus.CANCELADA) {
            throw new IllegalStateException("Registration already cancelled");
        }

        this.status = RegistrationStatus.CANCELADA;
    }
}
