package br.ifsp.demo.integration.api.registration;

import br.ifsp.demo.dto.RegistrationBody;
import br.ifsp.demo.dto.RegistrationResponse;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Inscrição em evento")
class RegisterToEventApiTest extends BaseApiTest {

    private static final String PASSWORD = "SenhaForte123";

    private String organizerToken;
    private String participanteToken;
    private UUID eventId;
    private UUID categoryId;

    @BeforeEach
    void prepararEventoComCategoria() {
        organizerToken = novoUsuario();
        participanteToken = novoUsuario();
        eventId = RegistrationApiFixture.createEvent(organizerToken,
                "Evento " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        categoryId = RegistrationApiFixture.createCategory(organizerToken, eventId, "Inteira", 50.0, 10);
    }

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
    }

    private Response inscrever(String token, UUID evento, Object corpo) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(corpo)
                .when()
                .post(BASE_PATH + "/events/" + evento + "/registrations");
    }

    private List<UUID> inscricoesAtivasDe(String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .queryParam("filter", "ATIVOS")
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .extract().jsonPath()
                .getList("registrationId", UUID.class);
    }

    @Test
    @DisplayName("Deve inscrever o participante, retornar 201 com o payload e listar a inscrição como ativa")
    void deveInscreverParticipante() {
        RegistrationResponse inscricao = inscrever(participanteToken, eventId, new RegistrationBody(categoryId))
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("eventId", equalTo(eventId.toString()))
                .body("categoryId", equalTo(categoryId.toString()))
                .body("status", equalTo("ATIVA"))
                .extract().as(RegistrationResponse.class);

        assertThat(inscricoesAtivasDe(participanteToken)).containsExactly(inscricao.id());
    }

    @Test
    @DisplayName("Deve permitir nova inscrição no mesmo evento depois de cancelar a anterior")
    void devePermitirReinscricaoAposCancelamento() {
        UUID primeira = RegistrationApiFixture.register(participanteToken, eventId, categoryId);
        RegistrationApiFixture.cancel(participanteToken, primeira);

        UUID nova = inscrever(participanteToken, eventId, new RegistrationBody(categoryId))
                .then()
                .statusCode(201)
                .extract().as(RegistrationResponse.class).id();

        assertThat(nova).isNotEqualTo(primeira);
        assertThat(inscricoesAtivasDe(participanteToken)).containsExactly(nova);
    }

    @Test
    @DisplayName("Deve retornar 409 ao inscrever novamente quem já tem inscrição ativa no evento")
    void deveRetornar409ComInscricaoDuplicada() {
        UUID primeira = RegistrationApiFixture.register(participanteToken, eventId, categoryId);

        inscrever(participanteToken, eventId, new RegistrationBody(categoryId))
                .then()
                .statusCode(409)
                .body("message", equalTo("User already registered in this event"));

        assertThat(inscricoesAtivasDe(participanteToken)).containsExactly(primeira);
    }

    @Test
    @DisplayName("Deve aceitar a última vaga da categoria e retornar 409 para a inscrição seguinte")
    void deveRetornar409QuandoCategoriaLotada() {
        UUID categoriaDeUmaVaga = RegistrationApiFixture.createCategory(organizerToken, eventId, "Vaga única", 30.0, 1);
        String outroParticipanteToken = novoUsuario();
        inscrever(participanteToken, eventId, new RegistrationBody(categoriaDeUmaVaga))
                .then()
                .statusCode(201);

        inscrever(outroParticipanteToken, eventId, new RegistrationBody(categoriaDeUmaVaga))
                .then()
                .statusCode(409)
                .body("message", equalTo("No vacancies left for this category"));

        assertThat(inscricoesAtivasDe(outroParticipanteToken)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar 403 quando o organizador tenta se inscrever no próprio evento")
    void deveRetornar403QuandoOrganizadorSeInscreve() {
        inscrever(organizerToken, eventId, new RegistrationBody(categoryId))
                .then()
                .statusCode(403)
                .body("message", equalTo("Event organizer cannot register to their own event"));

        assertThat(inscricoesAtivasDe(organizerToken)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar 400 quando a categoria não é informada")
    void deveRetornar400SemCategoria() {
        inscrever(participanteToken, eventId, new RegistrationBody(null))
                .then()
                .statusCode(400)
                .body("message", equalTo("Category is required"));

        assertThat(inscricoesAtivasDe(participanteToken)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id da categoria não é um UUID")
    void deveRetornar400ComIdDeCategoriaInvalido() {
        inscrever(participanteToken, eventId, "{\"categoryId\": \"nao-e-um-uuid\"}")
                .then()
                .statusCode(400);

        assertThat(inscricoesAtivasDe(participanteToken)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar 401 ao se inscrever sem token")
    void deveRetornar401SemToken() {
        given()
                .contentType("application/json")
                .body(new RegistrationBody(categoryId))
                .when()
                .post(BASE_PATH + "/events/" + eventId + "/registrations")
                .then()
                .statusCode(401);
    }
}
