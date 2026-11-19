package br.ifsp.demo.integration.api.event;

import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Cancelamento de evento (POST /events/{eventId}/cancel)")
class CancelEventApiTest extends BaseApiTest {

    private static final String PASSWORD = "123456";
    private static final String EVENTS_PATH = BASE_PATH + "/events";

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
    }

    @Test
    @DisplayName("Deve cancelar o evento do organizador e retornar 200 com status CANCELLED")
    void deveCancelarEventoDoOrganizador() {
        String token = novoUsuario();
        UUID eventId = RegistrationApiFixture.createEvent(token, "Evento a cancelar",
                RegistrationApiFixture.futureStart());

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .post(EVENTS_PATH + "/" + eventId + "/cancel")
        .then()
                .statusCode(200)
                .body("id", equalTo(eventId.toString()))
                .body("name", equalTo("Evento a cancelar"))
                .body("status", equalTo(EventStatus.CANCELLED.name()));

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get(EVENTS_PATH + "/mine")
        .then()
                .statusCode(200)
                .body("[0].status", equalTo(EventStatus.CANCELLED.name()));
    }

    @Test
    @DisplayName("Deve retornar 403 quando quem cancela não é o organizador do evento")
    void deveRetornar403QuandoNaoForOrganizador() {
        String tokenOrganizador = novoUsuario();
        String tokenOutroUsuario = novoUsuario();
        UUID eventId = RegistrationApiFixture.createEvent(tokenOrganizador, "Evento protegido",
                RegistrationApiFixture.futureStart());

        given()
                .header("Authorization", "Bearer " + tokenOutroUsuario)
        .when()
                .post(EVENTS_PATH + "/" + eventId + "/cancel")
        .then()
                .statusCode(403);

        given()
                .header("Authorization", "Bearer " + tokenOrganizador)
        .when()
                .get(EVENTS_PATH + "/mine")
        .then()
                .statusCode(200)
                .body("[0].status", equalTo(EventStatus.ACTIVE.name()));
    }

    @Test
    @DisplayName("Deve retornar 409 ao cancelar evento que possui inscrição ativa")
    void deveRetornar409QuandoHouverInscricaoAtiva() {
        String tokenOrganizador = novoUsuario();
        String tokenParticipante = novoUsuario();
        UUID eventId = RegistrationApiFixture.createEvent(tokenOrganizador, "Evento com inscritos",
                RegistrationApiFixture.futureStart());
        UUID categoryId = RegistrationApiFixture.createCategory(tokenOrganizador, eventId,
                "Geral " + UUID.randomUUID(), 50.0, 10);
        RegistrationApiFixture.register(tokenParticipante, eventId, categoryId);

        given()
                .header("Authorization", "Bearer " + tokenOrganizador)
        .when()
                .post(EVENTS_PATH + "/" + eventId + "/cancel")
        .then()
                .statusCode(409);

        given()
                .header("Authorization", "Bearer " + tokenOrganizador)
        .when()
                .get(EVENTS_PATH + "/mine")
        .then()
                .statusCode(200)
                .body("[0].status", equalTo(EventStatus.ACTIVE.name()));
    }

    @Test
    @DisplayName("Deve retornar 409 ao cancelar um evento que já está cancelado")
    void deveRetornar409AoCancelarEventoJaCancelado() {
        String token = novoUsuario();
        UUID eventId = RegistrationApiFixture.createEvent(token, "Evento cancelado duas vezes",
                RegistrationApiFixture.futureStart());
        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .post(EVENTS_PATH + "/" + eventId + "/cancel")
        .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .post(EVENTS_PATH + "/" + eventId + "/cancel")
        .then()
                .statusCode(409);
    }
}
