package br.ifsp.demo.integration.api.registration;

import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Cancelamento de inscricao")
class CancelRegistrationApiTest extends BaseApiTest {

    private static final String PASSWORD = "123456";

    private String participantToken;
    private UUID registrationId;

    @BeforeEach
    void prepararInscricaoAtiva() {
        String organizerToken = novoUsuario();
        participantToken = novoUsuario();

        UUID eventId = RegistrationApiFixture.createEvent(organizerToken,
                "Evento " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        UUID categoryId = RegistrationApiFixture.createCategory(organizerToken, eventId,
                "Geral " + UUID.randomUUID(), 50.0, 10);
        registrationId = RegistrationApiFixture.register(participantToken, eventId, categoryId);
    }

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
    }

    @Test
    @DisplayName("Deve cancelar inscricao ativa e retornar 204")
    void deveCancelarInscricaoAtiva() {
        given()
                .header("Authorization", "Bearer " + participantToken)
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/" + registrationId + "/cancel")
                .then()
                .statusCode(204)
                .body(emptyOrNullString());
    }

    @Test
    @DisplayName("Deve retornar 401 quando nao houver token")
    void deveRetornar401SemToken() {
        given()
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/" + registrationId + "/cancel")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Deve retornar 401 quando o token for invalido")
    void deveRetornar401ComTokenInvalido() {
        given()
                .header("Authorization", "Bearer token.invalido.qualquer")
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/" + registrationId + "/cancel")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Deve retornar 403 quando outro usuario tentar cancelar a inscricao")
    void deveRetornar403QuandoNaoForDono() {
        String outroUsuarioToken = novoUsuario();

        given()
                .header("Authorization", "Bearer " + outroUsuarioToken)
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/" + registrationId + "/cancel")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Deve retornar 409 ao cancelar uma inscricao ja cancelada")
    void deveRetornar409AoCancelarDuasVezes() {
        given()
                .header("Authorization", "Bearer " + participantToken)
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/" + registrationId + "/cancel")
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + participantToken)
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/" + registrationId + "/cancel")
                .then()
                .statusCode(409)
                .body("status", equalTo("CONFLICT"))
                .body("message", not(emptyOrNullString()));
    }

    @Test
    @DisplayName("Deve retornar 404 quando a inscricao nao existir")
    void deveRetornar404QuandoInscricaoNaoExiste() {
        given()
                .header("Authorization", "Bearer " + participantToken)
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/" + UUID.randomUUID() + "/cancel")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Deve retornar 400 quando o id da inscricao nao for um UUID")
    void deveRetornar400QuandoIdInvalido() {
        given()
                .header("Authorization", "Bearer " + participantToken)
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/nao-e-um-uuid/cancel")
                .then()
                .statusCode(400);
    }
}