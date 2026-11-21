package br.ifsp.demo.integration.api.category;

import br.ifsp.demo.dto.CategoryResponse;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import br.ifsp.demo.support.RegistrationDbFixture;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Remoção de categoria")
class DeleteCategoryApiTest extends BaseApiTest {

    private static final String PASSWORD = "SenhaForte123";

    @Autowired
    private JdbcTemplate jdbc;

    private String organizerToken;
    private UUID organizerId;
    private UUID eventId;
    private CategoryResponse categoria;

    @BeforeEach
    void prepararEventoComCategoria() {
        String organizerEmail = EntityBuilder.randomEmail();
        organizerToken = registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                organizerEmail, PASSWORD);
        organizerId = userRepository.findByEmail(organizerEmail).orElseThrow().getId();
        eventId = RegistrationApiFixture.createEvent(organizerToken,
                "Evento " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        UUID categoryId = RegistrationApiFixture.createCategory(organizerToken, eventId, "Inteira", 50.0, 10);
        categoria = new CategoryResponse(categoryId, "Inteira", 10, 50.0);
    }

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
    }

    private Response removerCategoria(String token, UUID evento, Object categoryId) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(BASE_PATH + "/events/" + evento + "/categories/" + categoryId);
    }

    private List<CategoryResponse> categoriasDoEvento(UUID evento) {
        return RegistrationApiFixture.listCategories(organizerToken, evento);
    }

    @Test
    @DisplayName("Deve remover categoria sem inscrições, retornar 204 e desvinculá-la do evento")
    void deveRemoverCategoriaSemInscricoes() {
        removerCategoria(organizerToken, eventId, categoria.id())
                .then()
                .statusCode(204)
                .body(emptyOrNullString());

        assertThat(categoriasDoEvento(eventId)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar 403 ao remover categoria com inscrição ativa, mantendo-a no evento")
    void deveRetornar403ComInscricaoAtiva() {
        RegistrationApiFixture.register(novoUsuario(), eventId, categoria.id());

        removerCategoria(organizerToken, eventId, categoria.id())
                .then()
                .statusCode(403)
                .body("message", equalTo("Category has registrations"));

        assertThat(categoriasDoEvento(eventId)).containsExactly(categoria);
    }

    @Test
    @DisplayName("Deve remover categoria cujas inscrições foram todas canceladas e retornar 204")
    void deveRemoverCategoriaComInscricoesSomenteCanceladas() {
        String participanteToken = novoUsuario();
        UUID inscricao = RegistrationApiFixture.register(participanteToken, eventId, categoria.id());
        RegistrationApiFixture.cancel(participanteToken, inscricao);

        removerCategoria(organizerToken, eventId, categoria.id())
                .then()
                .statusCode(204);

        assertThat(categoriasDoEvento(eventId)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar 403 quando quem remove não é o organizador do evento, mantendo a categoria")
    void deveRetornar403QuandoNaoEOrganizador() {
        removerCategoria(novoUsuario(), eventId, categoria.id())
                .then()
                .statusCode(403)
                .body("message", equalTo("Only the event organizer can delete categories"));

        assertThat(categoriasDoEvento(eventId)).containsExactly(categoria);
    }

    @Test
    @DisplayName("Deve retornar 401 sem token, mantendo a categoria")
    void deveRetornar401SemToken() {
        given()
                .when()
                .delete(BASE_PATH + "/events/" + eventId + "/categories/" + categoria.id())
                .then()
                .statusCode(401);

        assertThat(categoriasDoEvento(eventId)).containsExactly(categoria);
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id da categoria não é um UUID")
    void deveRetornar400ComIdDeCategoriaInvalido() {
        removerCategoria(organizerToken, eventId, "nao-e-um-uuid")
                .then()
                .statusCode(400);

        assertThat(categoriasDoEvento(eventId)).containsExactly(categoria);
    }

    @Test
    @DisplayName("Deve retornar 404 ao remover categoria de evento inexistente")
    void deveRetornar404ComEventoInexistente() {
        UUID eventoInexistente = UUID.randomUUID();

        removerCategoria(organizerToken, eventoInexistente, categoria.id())
                .then()
                .statusCode(404)
                .body("message", equalTo("Event not found: " + eventoInexistente));
    }

    @Test
    @DisplayName("Deve retornar 404 ao remover pelo evento errado uma categoria que pertence a outro evento")
    void deveRetornar404ComCategoriaDeOutroEvento() {
        UUID outroEvento = RegistrationApiFixture.createEvent(organizerToken,
                "Evento " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        UUID categoriaDoOutroEvento = RegistrationApiFixture.createCategory(organizerToken, outroEvento, "VIP", 200.0, 5);

        removerCategoria(organizerToken, eventId, categoriaDoOutroEvento)
                .then()
                .statusCode(404)
                .body("message", equalTo("Category not found: " + categoriaDoOutroEvento));

        assertThat(categoriasDoEvento(outroEvento))
                .containsExactly(new CategoryResponse(categoriaDoOutroEvento, "VIP", 5, 200.0));
    }

    @Test
    @DisplayName("Deve retornar 404 ao remover uma categoria que já foi removida")
    void deveRetornar404AoRemoverCategoriaJaRemovida() {
        removerCategoria(organizerToken, eventId, categoria.id())
                .then()
                .statusCode(204);

        removerCategoria(organizerToken, eventId, categoria.id())
                .then()
                .statusCode(404)
                .body("message", equalTo("Category not found: " + categoria.id()));
    }

    @Test
    @DisplayName("Deve retornar 409 ao remover categoria de evento que já começou, mantendo-a")
    void deveRetornar409ComEventoJaIniciado() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS);
        RegistrationDbFixture fixture = new RegistrationDbFixture(jdbc);
        UUID eventoIniciado = fixture.insertEvent(inicio, inicio.plusHours(3), "ACTIVE", organizerId);
        UUID categoriaDoEventoIniciado = fixture.insertCategory(eventoIniciado, "Inteira", 10, 50.0);

        removerCategoria(organizerToken, eventoIniciado, categoriaDoEventoIniciado)
                .then()
                .statusCode(409)
                .body("message", equalTo("Event has already started"));

        assertThat(categoriasDoEvento(eventoIniciado))
                .containsExactly(new CategoryResponse(categoriaDoEventoIniciado, "Inteira", 10, 50.0));
    }
}
