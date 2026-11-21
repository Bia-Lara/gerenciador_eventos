package br.ifsp.demo.integration.persistence;

import br.ifsp.demo.domain.Category;
import br.ifsp.demo.domain.Event;
import br.ifsp.demo.infrastructure.repository.JdbcCategoryRepository;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcCategoryRepositoryTest {

    @Autowired
    private JdbcCategoryRepository sut;

    @Autowired
    private JdbcTemplate jdbc;

    private RegistrationDbFixture fixture;
    private LocalDateTime inicio;
    private UUID eventId;

    @BeforeEach
    void prepararEvento() {
        fixture = new RegistrationDbFixture(jdbc);
        inicio = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        eventId = fixture.insertEvent(inicio, inicio.plusHours(3), "ACTIVE");
    }

    private Event eventoGravado(UUID id) {
        return Event.restore(id, "Evento " + id, inicio, inicio.plusHours(3), UUID.randomUUID());
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("save deve gravar todos os campos da categoria vinculada ao evento")
    void saveDeveGravarTodosOsCampos() {
        Category categoria = eventoGravado(eventId).addCategory("Inteira", 40, 75.5);

        sut.save(categoria);

        Map<String, Object> linha = jdbc.queryForMap(
                "SELECT event_id, name, capacity, price FROM category WHERE id = ?", categoria.getId().toString());
        assertThat(linha.get("event_id")).isEqualTo(eventId.toString());
        assertThat(linha.get("name")).isEqualTo("Inteira");
        assertThat(linha.get("capacity")).isEqualTo(40);
        assertThat(linha.get("price")).isEqualTo(75.5);
    }
}
