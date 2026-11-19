package br.ifsp.demo.integration.persistence;

import br.ifsp.demo.domain.Registration;
import br.ifsp.demo.domain.enumerations.RegistrationStatus;
import br.ifsp.demo.infrastructure.repository.JdbcRegistrationRepository;
import br.ifsp.demo.infrastructure.security.user.JpaUserRepository;
import br.ifsp.demo.infrastructure.security.user.User;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationDbFixture;
import br.ifsp.demo.domain.Category;
import br.ifsp.demo.exception.UserNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JdbcRegistrationRepositoryTest {

    @Autowired
    private JdbcRegistrationRepository sut;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegistrationDbFixture fixture;

    @BeforeEach
    void prepararFixture() {
        fixture = new RegistrationDbFixture(jdbc);
    }

    @AfterEach
    void limparUsuarios() {
        userRepository.deleteAll();
    }

    private User salvarUsuario() {
        return userRepository.save(EntityBuilder.createRandomUser(passwordEncoder.encode("Teste123@")));
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findById deve retornar a inscricao com todos os campos mapeados")
    void findByIdDeveRetornarInscricaoComTodosOsCamposMapeados() {
        User user = salvarUsuario();
        LocalDateTime start = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime end = start.plusHours(3);
        UUID eventId = fixture.insertEvent(start, end, "ACTIVE");
        UUID categoryId = fixture.insertCategory(eventId, "Geral", 40, 75.5);
        UUID registrationId = fixture.insertRegistration(user.getId(), categoryId, "ATIVA");

        Optional<Registration> found = sut.findById(registrationId);

        assertThat(found).isPresent();
        Registration registration = found.get();
        assertThat(registration.getId()).isEqualTo(registrationId);
        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.ATIVA);
        assertThat(registration.getUser().getId()).isEqualTo(user.getId());
        assertThat(registration.getCategory().getId()).isEqualTo(categoryId);
        assertThat(registration.getCategory().getName()).isEqualTo("Geral");
        assertThat(registration.getCategory().getCapacity()).isEqualTo(40);
        assertThat(registration.getCategory().getPrice()).isEqualTo(75.5);
        assertThat(registration.getCategory().getEvent().getId()).isEqualTo(eventId);
        assertThat(registration.getCategory().getEvent().getStartDateTime()).isEqualTo(start);
        assertThat(registration.getCategory().getEvent().getEndDateTime()).isEqualTo(end);
        assertThat(registration.getCategory().getEvent().getOrganizerId()).isNotNull();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findById com id inexistente deve retornar vazio")
    void findByIdComIdInexistenteDeveRetornarVazio() {
        Optional<Registration> found = sut.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @ParameterizedTest(name = "evento {0}")
    @ValueSource(strings = {"CANCELLED", "COMPLETED"})
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findById deve refletir o status do evento gravado no banco")
    void findByIdDeveRefletirStatusDoEvento(String statusDoEvento) {
        User user = salvarUsuario();
        LocalDateTime start = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        UUID eventId = fixture.insertEvent(start, start.plusHours(3), statusDoEvento);
        UUID categoryId = fixture.insertCategory(eventId, "Geral", 40, 75.5);
        UUID registrationId = fixture.insertRegistration(user.getId(), categoryId, "ATIVA");

        Registration registration = sut.findById(registrationId).orElseThrow();

        assertThat(registration.getCategory().getEvent().getStatus().name()).isEqualTo(statusDoEvento);
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findById deve carregar todas as categorias do evento e nao apenas a da inscricao")
    void findByIdDeveCarregarTodasAsCategoriasDoEvento() {
        User user = salvarUsuario();
        LocalDateTime start = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        UUID eventId = fixture.insertEvent(start, start.plusHours(3), "ACTIVE");
        UUID categoriaDoUsuario = fixture.insertCategory(eventId, "Geral", 40, 75.5);
        fixture.insertCategory(eventId, "VIP", 10, 200.0);
        UUID registrationId = fixture.insertRegistration(user.getId(), categoriaDoUsuario, "ATIVA");

        Registration registration = sut.findById(registrationId).orElseThrow();

        assertThat(registration.getCategory().getEvent().getCategories())
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Geral", "VIP");
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findById de inscricao cujo dono nao existe deve lancar UserNotFoundException")
    void findByIdComDonoInexistenteDeveLancarUserNotFoundException() {
        LocalDateTime start = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        UUID eventId = fixture.insertEvent(start, start.plusHours(3), "ACTIVE");
        UUID categoryId = fixture.insertCategory(eventId, "Geral", 40, 75.5);
        UUID registrationId = fixture.insertRegistration(UUID.randomUUID(), categoryId, "ATIVA");

        assertThatThrownBy(() -> sut.findById(registrationId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("save deve persistir o cancelamento sem duplicar nem alterar outras inscricoes")
    void saveDevePersistirCancelamentoSemAfetarOutrasInscricoes() {
        User user = salvarUsuario();
        User outro = salvarUsuario();
        LocalDateTime start = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        UUID eventId = fixture.insertEvent(start, start.plusHours(3), "ACTIVE");
        UUID categoryId = fixture.insertCategory(eventId, "Geral", 40, 75.5);
        UUID registrationId = fixture.insertRegistration(user.getId(), categoryId, "ATIVA");
        UUID outraId = fixture.insertRegistration(outro.getId(), categoryId, "ATIVA");

        Registration registration = sut.findById(registrationId).orElseThrow();
        registration.cancel(user.getId());
        sut.save(registration);

        assertThat(sut.findById(registrationId).orElseThrow().getStatus())
                .isEqualTo(RegistrationStatus.CANCELADA);
        assertThat(sut.findById(outraId).orElseThrow().getStatus())
                .isEqualTo(RegistrationStatus.ATIVA);
        Integer linhas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM registration WHERE category_id = ?",
                Integer.class, categoryId.toString());
        assertThat(linhas).isEqualTo(2);
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("save de inscricao inexistente nao deve gravar nada nem lancar erro")
    void saveDeInscricaoInexistenteNaoDeveGravarNada() {
        User user = salvarUsuario();
        LocalDateTime start = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
        UUID eventId = fixture.insertEvent(start, start.plusHours(3), "ACTIVE");
        UUID categoryId = fixture.insertCategory(eventId, "Geral", 40, 75.5);
        UUID registrationId = fixture.insertRegistration(user.getId(), categoryId, "ATIVA");
        Registration registration = sut.findById(registrationId).orElseThrow();
        registration.cancel(user.getId());
        jdbc.update("DELETE FROM registration WHERE id = ?", registrationId.toString());

        sut.save(registration);

        assertThat(sut.findById(registrationId)).isEmpty();
        Integer linhas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM registration WHERE id = ?",
                Integer.class, registrationId.toString());
        assertThat(linhas).isZero();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("findByUserId de usuario sem inscricoes deve retornar lista vazia")
    void findByUserIdSemInscricoesDeveRetornarListaVazia() {
        User user = salvarUsuario();

        List<Registration> registrations = sut.findByUserId(user.getId());

        assertThat(registrations).isEmpty();
    }
}