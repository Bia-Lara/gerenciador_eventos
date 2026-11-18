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

    @Test
    @DisplayName("Deve retornar somente as inscricoes canceladas quando o filtro for INATIVOS")
    void deveRetornarSomenteCanceladasComFiltroInativos() {
        String organizerToken = novoUsuario();
        String participantToken = novoUsuario();
        inscreverEmNovoEvento(organizerToken, participantToken);
        UUID cancelada = inscreverEmNovoEvento(organizerToken, participantToken);
        cancelar(participantToken, cancelada);

        given()
                .header("Authorization", "Bearer " + participantToken)
                .queryParam("filter", "INATIVOS")
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("registrationId", contains(cancelada.toString()))
                .body("status", contains("CANCELADA"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando o filtro for invalido")
    void deveRetornar400QuandoFiltroInvalido() {
        given()
                .header("Authorization", "Bearer " + novoUsuario())
                .queryParam("filter", "xyz")
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Deve retornar 401 quando nao houver token")
    void deveRetornar401SemToken() {
        given()
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Deve retornar somente as inscricoes do proprio usuario")
    void deveRetornarSomenteInscricoesDoProprioUsuario() {
        String organizerToken = novoUsuario();
        String primeiroToken = novoUsuario();
        String segundoToken = novoUsuario();
        UUID inscricaoDoPrimeiro = inscreverEmNovoEvento(organizerToken, primeiroToken);
        UUID inscricaoDoSegundo = inscreverEmNovoEvento(organizerToken, segundoToken);

        given()
                .header("Authorization", "Bearer " + primeiroToken)
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("registrationId", contains(inscricaoDoPrimeiro.toString()));

        given()
                .header("Authorization", "Bearer " + segundoToken)
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("registrationId", contains(inscricaoDoSegundo.toString()));
    }

    @Test
    @DisplayName("Deve listar a inscricao cancelada e a nova inscricao ativa do mesmo evento")
    void deveListarInscricaoCanceladaEReinscricaoAtiva() {
        String organizerToken = novoUsuario();
        String participantToken = novoUsuario();
        UUID eventId = RegistrationApiFixture.createEvent(organizerToken,
                "Evento " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        UUID categoryId = RegistrationApiFixture.createCategory(organizerToken, eventId,
                "Geral " + UUID.randomUUID(), 50.0, 10);

        UUID primeira = RegistrationApiFixture.register(participantToken, eventId, categoryId);
        cancelar(participantToken, primeira);
        UUID segunda = RegistrationApiFixture.register(participantToken, eventId, categoryId);

        given()
                .header("Authorization", "Bearer " + participantToken)
                .when()
                .get(BASE_PATH + "/registrations/details")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("registrationId", containsInAnyOrder(primeira.toString(), segunda.toString()))
                .body("find { it.registrationId == '" + primeira + "' }.status", equalTo("CANCELADA"))
                .body("find { it.registrationId == '" + segunda + "' }.status", equalTo("ATIVA"));
    }
}