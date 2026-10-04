package br.ifsp.demo.domain;

import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.infrastructure.security.user.User;

import java.util.UUID;

public class Registration {
    private final UUID id;
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
}
