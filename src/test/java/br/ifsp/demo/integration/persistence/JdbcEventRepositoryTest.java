package br.ifsp.demo.integration.persistence;

import br.ifsp.demo.domain.Event;
import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.infrastructure.repository.JdbcEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findByOrganizerId deve retornar somente os eventos do organizador, inclusive os cancelados")
    void findByOrganizerIdDeveRetornarSomenteEventosDoOrganizador() {
        UUID organizerId = UUID.randomUUID();
        LocalDateTime start = futureStart();
        Event ativo = new Event("Ativo", start, start.plusHours(2), organizerId);
        Event cancelado = new Event("Cancelado", start.plusDays(1), start.plusDays(1).plusHours(2), organizerId);
        Event deOutro = new Event("De outro", start, start.plusHours(2), UUID.randomUUID());
        sut.create(ativo);
        sut.create(cancelado);
        sut.create(deOutro);
        cancelado.cancel(organizerId);
        sut.save(cancelado);

        List<Event> events = sut.findByOrganizerId(organizerId);

        assertThat(events).extracting(Event::getId)
                .containsExactlyInAnyOrder(ativo.getId(), cancelado.getId());
        assertThat(events).extracting(Event::getStatus)
                .containsExactlyInAnyOrder(EventStatus.ACTIVE, EventStatus.CANCELLED);
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findByOrganizerId deve retornar lista vazia quando o organizador não tem eventos")
    void findByOrganizerIdSemEventosDeveRetornarListaVazia() {
        List<Event> events = sut.findByOrganizerId(UUID.randomUUID());

        assertThat(events).isEmpty();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findAvailableForRegistration deve retornar só eventos ativos, futuros e de outros organizadores")
    void findAvailableForRegistrationDeveAplicarOsFiltros() {
        UUID consultante = UUID.randomUUID();
        UUID outroOrganizador = UUID.randomUUID();
        LocalDateTime start = futureStart();
        Event disponivel = new Event("Disponível", start, start.plusHours(2), outroOrganizador);
        Event proprio = new Event("Próprio", start, start.plusHours(2), consultante);
        Event cancelado = new Event("Cancelado", start, start.plusHours(2), outroOrganizador);
        Event passado = Event.restore(UUID.randomUUID(), "Passado",
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2).plusHours(2), outroOrganizador);
        sut.create(disponivel);
        sut.create(proprio);
        sut.create(cancelado);
        sut.create(passado);
        cancelado.cancel(outroOrganizador);
        sut.save(cancelado);

        List<Event> events = sut.findAvailableForRegistration(consultante, LocalDateTime.now());

        assertThat(events).extracting(Event::getId)
                .contains(disponivel.getId())
                .doesNotContain(proprio.getId(), cancelado.getId(), passado.getId());
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findAvailableForRegistration deve ordenar os eventos pela data de início")
    void findAvailableForRegistrationDeveOrdenarPelaDataDeInicio() {
        UUID organizador = UUID.randomUUID();
        LocalDateTime base = futureStart();
        Event tarde = new Event("Tarde", base.plusDays(3), base.plusDays(3).plusHours(2), organizador);
        Event cedo = new Event("Cedo", base.plusDays(1), base.plusDays(1).plusHours(2), organizador);
        Event meio = new Event("Meio", base.plusDays(2), base.plusDays(2).plusHours(2), organizador);
        sut.create(tarde);
        sut.create(cedo);
        sut.create(meio);

        List<Event> events = sut.findAvailableForRegistration(UUID.randomUUID(), LocalDateTime.now());

        assertThat(events).extracting(Event::getId)
                .filteredOn(id -> List.of(tarde.getId(), cedo.getId(), meio.getId()).contains(id))
                .containsExactly(cedo.getId(), meio.getId(), tarde.getId());
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findByDate deve incluir eventos do dia (de 00:00 a 23:59:59) e excluir os dias vizinhos")
    void findByDateDeveRespeitarOsLimitesDoDia() {
        UUID organizador = UUID.randomUUID();
        LocalDate dia = LocalDate.now().plusDays(500);
        Event inicioDoDia = new Event("Início do dia", dia.atStartOfDay(), dia.atTime(2, 0), organizador);
        Event fimDoDia = new Event("Fim do dia", dia.atTime(23, 59, 59), dia.plusDays(1).atTime(2, 0), organizador);
        Event diaSeguinte = new Event("Dia seguinte", dia.plusDays(1).atStartOfDay(), dia.plusDays(1).atTime(2, 0), organizador);
        Event diaAnterior = new Event("Dia anterior", dia.minusDays(1).atTime(23, 59, 59), dia.atTime(1, 0), organizador);
        sut.create(inicioDoDia);
        sut.create(fimDoDia);
        sut.create(diaSeguinte);
        sut.create(diaAnterior);

        List<Event> events = sut.findByDate(dia);

        assertThat(events).extracting(Event::getId)
                .containsExactlyInAnyOrder(inicioDoDia.getId(), fimDoDia.getId());
    }
}
