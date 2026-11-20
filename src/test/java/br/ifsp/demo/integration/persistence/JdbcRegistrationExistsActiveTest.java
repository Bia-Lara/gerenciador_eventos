package br.ifsp.demo.integration.persistence;

import br.ifsp.demo.infrastructure.repository.JdbcRegistrationRepository;
import br.ifsp.demo.support.RegistrationDbFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcRegistrationExistsActiveTest {

    @Autowired
    private JdbcRegistrationRepository sut;

    @Autowired
    private JdbcTemplate jdbc;

    private RegistrationDbFixture fixture;

    @BeforeEach
    void prepararFixture() {
        fixture = new RegistrationDbFixture(jdbc);
    }

    private UUID novoEventoComCategoria() {
        LocalDateTime start = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        return fixture.insertEvent(start, start.plusHours(3), "ACTIVE");
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("existsActiveByEventId deve retornar verdadeiro quando o evento tem inscrição ativa")
    void existsActiveByEventIdDeveRetornarVerdadeiroComInscricaoAtiva() {
        UUID eventId = novoEventoComCategoria();
        UUID categoryId = fixture.insertCategory(eventId, "Geral", 40, 50.0);
        fixture.insertRegistration(UUID.randomUUID(), categoryId, "ATIVA");

        boolean exists = sut.existsActiveByEventId(eventId);

        assertThat(exists).isTrue();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("existsActiveByEventId deve retornar falso quando todas as inscrições do evento estão canceladas")
    void existsActiveByEventIdDeveRetornarFalsoComSomenteInscricoesCanceladas() {
        UUID eventId = novoEventoComCategoria();
        UUID categoryId = fixture.insertCategory(eventId, "Geral", 40, 50.0);
        fixture.insertRegistration(UUID.randomUUID(), categoryId, "CANCELADA");
        fixture.insertRegistration(UUID.randomUUID(), categoryId, "CANCELADA");

        boolean exists = sut.existsActiveByEventId(eventId);

        assertThat(exists).isFalse();
    }
}
