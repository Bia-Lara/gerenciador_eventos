package br.ifsp.demo.integration.api.registration;

import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.contains;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import java.time.LocalDateTime;
import java.util.UUID;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Consulta de inscricoes")
class QueryRegistrationApiTest extends BaseApiTest {

    private static final String PASSWORD = "123456";

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
    }

    private UUID inscreverEmNovoEvento(String organizerToken, String participantToken) {
        UUID eventId = RegistrationApiFixture.createEvent(organizerToken,
                "Evento " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        UUID categoryId = RegistrationApiFixture.createCategory(organizerToken, eventId,
                "Geral " + UUID.randomUUID(), 50.0, 10);
        return RegistrationApiFixture.register(participantToken, eventId, categoryId);
    }

    private void cancelar(String token, UUID registrationId) {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .when()
                .post(BASE_PATH + "/events/registrations/" + registrationId + "/cancel")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("Deve retornar lista vazia para usuario sem inscricoes")
    void deveRetornarListaVaziaSemInscricoes() {
        given()
                .header("Authorization", "Bearer " + novoUsuario())
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("$", empty());
    }

    @Test
    @DisplayName("Deve retornar os dados completos da inscricao ativa")
    void deveRetornarDadosCompletosDaInscricao() {
        String organizerToken = novoUsuario();
        String participantToken = novoUsuario();
        String eventName = "Evento " + UUID.randomUUID();
        String categoryName = "Geral " + UUID.randomUUID();
        LocalDateTime start = RegistrationApiFixture.futureStart();

        UUID eventId = RegistrationApiFixture.createEvent(organizerToken, eventName, start);
        UUID categoryId = RegistrationApiFixture.createCategory(organizerToken, eventId, categoryName, 50.0, 10);
        UUID registrationId = RegistrationApiFixture.register(participantToken, eventId, categoryId);

        given()
                .header("Authorization", "Bearer " + participantToken)
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].registrationId", equalTo(registrationId.toString()))
                .body("[0].eventId", equalTo(eventId.toString()))
                .body("[0].eventName", equalTo(eventName))
                .body("[0].categoryId", equalTo(categoryId.toString()))
                .body("[0].categoryName", equalTo(categoryName))
                .body("[0].price", equalTo(50.0f))
                .body("[0].status", equalTo("ATIVA"))
                .body("[0].eventStatus", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Deve retornar ativas e canceladas quando o filtro for TODOS ou nao for informado")
    void deveRetornarTodasAsInscricoesComFiltroTodos() {
        String organizerToken = novoUsuario();
        String participantToken = novoUsuario();
        UUID ativa = inscreverEmNovoEvento(organizerToken, participantToken);
        UUID cancelada = inscreverEmNovoEvento(organizerToken, participantToken);
        cancelar(participantToken, cancelada);

        given()
                .header("Authorization", "Bearer " + participantToken)
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("registrationId", containsInAnyOrder(ativa.toString(), cancelada.toString()));

        given()
                .header("Authorization", "Bearer " + participantToken)
                .queryParam("filter", "TODOS")
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    @DisplayName("Deve retornar somente as inscricoes ativas quando o filtro for ATIVOS")
    void deveRetornarSomenteAtivasComFiltroAtivos() {
        String organizerToken = novoUsuario();
        String participantToken = novoUsuario();
        UUID ativa = inscreverEmNovoEvento(organizerToken, participantToken);
        UUID cancelada = inscreverEmNovoEvento(organizerToken, participantToken);
        cancelar(participantToken, cancelada);

        given()
                .header("Authorization", "Bearer " + participantToken)
                .queryParam("filter", "ATIVOS")
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("registrationId", contains(ativa.toString()))
                .body("status", contains("ATIVA"));
    }
}