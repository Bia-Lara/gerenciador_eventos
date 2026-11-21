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
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private List<String> nomesDasCategoriasDoEvento(UUID evento) {
        return jdbc.queryForList("SELECT name FROM category WHERE event_id = ?", String.class, evento.toString());
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

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("o banco deve recusar duas categorias com o mesmo nome no mesmo evento (índice único)")
    void bancoDeveRecusarNomeRepetidoNoMesmoEvento() {
        sut.save(eventoGravado(eventId).addCategory("Inteira", 40, 75.5));
        Category repetida = eventoGravado(eventId).addCategory("Inteira", 10, 20.0);

        assertThatThrownBy(() -> sut.save(repetida))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("UNIQUE constraint failed: category.event_id, category.name");

        assertThat(nomesDasCategoriasDoEvento(eventId)).containsExactly("Inteira");
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("o banco deve aceitar o mesmo nome de categoria em eventos diferentes")
    void bancoDeveAceitarMesmoNomeEmEventosDiferentes() {
        UUID outroEvento = fixture.insertEvent(inicio, inicio.plusHours(3), "ACTIVE");

        sut.save(eventoGravado(eventId).addCategory("Inteira", 40, 75.5));
        sut.save(eventoGravado(outroEvento).addCategory("Inteira", 10, 20.0));

        assertThat(nomesDasCategoriasDoEvento(eventId)).containsExactly("Inteira");
        assertThat(nomesDasCategoriasDoEvento(outroEvento)).containsExactly("Inteira");
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("o banco deve recusar nomes de categoria que só diferem em maiúsculas no mesmo evento")
    void bancoDeveRecusarNomeQueSoDifereEmMaiusculas() {
        sut.save(eventoGravado(eventId).addCategory("Inteira", 40, 75.5));
        Category emMaiusculas = eventoGravado(eventId).addCategory("INTEIRA", 10, 20.0);

        assertThatThrownBy(() -> sut.save(emMaiusculas))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("UNIQUE constraint failed");

        assertThat(nomesDasCategoriasDoEvento(eventId)).containsExactly("Inteira");
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("save deve gravar literalmente um nome com SQL injection, sem afetar outras categorias")
    void saveDeveGravarNomeComSqlInjectionLiteralmente() {
        String nomeMalicioso = "x'); DELETE FROM category; --";
        sut.save(eventoGravado(eventId).addCategory("Inteira", 40, 75.5));

        sut.save(eventoGravado(eventId).addCategory(nomeMalicioso, 10, 20.0));

        assertThat(nomesDasCategoriasDoEvento(eventId)).containsExactlyInAnyOrder("Inteira", nomeMalicioso);
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("delete deve remover somente a categoria informada")
    void deleteDeveRemoverSomenteACategoriaInformada() {
        Category inteira = eventoGravado(eventId).addCategory("Inteira", 40, 75.5);
        sut.save(inteira);
        sut.save(eventoGravado(eventId).addCategory("Meia", 40, 37.75));

        sut.delete(inteira);

        assertThat(nomesDasCategoriasDoEvento(eventId)).containsExactly("Meia");
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("o banco deve recusar remover categoria que tem inscrições (chave estrangeira)")
    void bancoDeveRecusarRemoverCategoriaComInscricoes() {
        Category inteira = eventoGravado(eventId).addCategory("Inteira", 40, 75.5);
        sut.save(inteira);
        fixture.insertRegistration(UUID.randomUUID(), inteira.getId(), "ATIVA");

        assertThatThrownBy(() -> sut.delete(inteira))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("FOREIGN KEY constraint failed");

        assertThat(nomesDasCategoriasDoEvento(eventId)).containsExactly("Inteira");
    }
}
