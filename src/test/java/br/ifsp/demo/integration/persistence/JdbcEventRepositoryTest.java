package br.ifsp.demo.integration.persistence;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.infrastructure.repository.JdbcEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcEventRepositoryTest {

    @Autowired
    private JdbcEventRepository sut;

    private static LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("create deve gravar o evento e findById deve devolver com todos os campos mapeados")
    void createDeveGravarEventoQueFindByIdRecupera() {
        UUID organizerId = UUID.randomUUID();
        LocalDateTime start = futureStart();
        LocalDateTime end = start.plusHours(3);
        Event event = new Event("Feira de ideias", start, end, organizerId);

        sut.create(event);
        Optional<Event> found = sut.findById(event.getId());

        assertThat(found).isPresent();
        Event saved = found.get();
        assertThat(saved.getId()).isEqualTo(event.getId());
        assertThat(saved.getName()).isEqualTo("Feira de ideias");
        assertThat(saved.getStartDateTime()).isEqualTo(start);
        assertThat(saved.getEndDateTime()).isEqualTo(end);
        assertThat(saved.getOrganizerId()).isEqualTo(organizerId);
        assertThat(saved.getStatus()).isEqualTo(EventStatus.ACTIVE);
        assertThat(saved.getCategories()).isEmpty();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findById com id inexistente deve retornar vazio")
    void findByIdComIdInexistenteDeveRetornarVazio() {
        Optional<Event> found = sut.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("save deve atualizar nome, datas e status do evento sem criar um novo registro")
    void saveDeveAtualizarEventoExistente() {
        UUID organizerId = UUID.randomUUID();
        LocalDateTime start = futureStart();
        Event event = new Event("Nome original", start, start.plusHours(2), organizerId);
        sut.create(event);
        LocalDateTime novoInicio = start.plusDays(5);
        LocalDateTime novoFim = novoInicio.plusHours(4);
        event.edit(organizerId, "Nome atualizado", novoInicio, novoFim);
        event.cancel(organizerId);

        sut.save(event);
        Event saved = sut.findById(event.getId()).orElseThrow();

        assertThat(saved.getName()).isEqualTo("Nome atualizado");
        assertThat(saved.getStartDateTime()).isEqualTo(novoInicio);
        assertThat(saved.getEndDateTime()).isEqualTo(novoFim);
        assertThat(saved.getStatus()).isEqualTo(EventStatus.CANCELLED);
        assertThat(sut.findByOrganizerId(organizerId)).hasSize(1);
    }
}
