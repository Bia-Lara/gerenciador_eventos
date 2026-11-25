package br.ifsp.demo.integration.api.event;

import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Listagem de eventos disponíveis (GET /events/available)")
class ListAvailableEventsApiTest extends BaseApiTest {

    private static final String PASSWORD = "123456";
    private static final String AVAILABLE_PATH = BASE_PATH + "/events/available";

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
    }

    @Test
    @DisplayName("Deve listar eventos de outros organizadores com capacidade e inscrições ativas, sem o próprio evento")
    void deveListarEventosDeOutrosOrganizadores() {
        String tokenConsultante = novoUsuario();
        String tokenOrganizador = novoUsuario();
        String tokenParticipante = novoUsuario();

        UUID eventoProprio = RegistrationApiFixture.createEvent(tokenConsultante,
                "Próprio " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        String nomeDoOutroEvento = "Outro " + UUID.randomUUID();
        UUID outroEvento = RegistrationApiFixture.createEvent(tokenOrganizador,
                nomeDoOutroEvento, RegistrationApiFixture.futureStart());
        UUID categoria = RegistrationApiFixture.createCategory(tokenOrganizador, outroEvento,
                "Geral " + UUID.randomUUID(), 50.0, 10);
        RegistrationApiFixture.register(tokenParticipante, outroEvento, categoria);

        String eventoDoOutro = "find { it.id == '" + outroEvento + "' }";
        given()
                .header("Authorization", "Bearer " + tokenConsultante)
        .when()
                .get(AVAILABLE_PATH)
        .then()
                .statusCode(200)
                .body("id", hasItem(outroEvento.toString()))
                .body("id", not(hasItem(eventoProprio.toString())))
                .body(eventoDoOutro + ".name", equalTo(nomeDoOutroEvento))
                .body(eventoDoOutro + ".categories", hasSize(1))
                .body(eventoDoOutro + ".totalCapacity", equalTo(10))
                .body(eventoDoOutro + ".activeRegistrations", equalTo(1));
    }

    @Test
    @DisplayName("Não deve listar evento cancelado")
    void naoDeveListarEventoCancelado() {
        String tokenConsultante = novoUsuario();
        String tokenOrganizador = novoUsuario();

        UUID eventoAtivo = RegistrationApiFixture.createEvent(tokenOrganizador,
                "Ativo " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        UUID eventoCancelado = RegistrationApiFixture.createEvent(tokenOrganizador,
                "Cancelado " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        given()
                .header("Authorization", "Bearer " + tokenOrganizador)
        .when()
                .post(BASE_PATH + "/events/" + eventoCancelado + "/cancel")
        .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + tokenConsultante)
        .when()
                .get(AVAILABLE_PATH)
        .then()
                .statusCode(200)
                .body("id", hasItem(eventoAtivo.toString()))
                .body("id", not(hasItem(eventoCancelado.toString())));
    }

    @Test
    @DisplayName("Deve retornar 401 quando não houver token")
    void deveRetornar401SemToken() {
        given()
        .when()
                .get(AVAILABLE_PATH)
        .then()
                .statusCode(401);
    }
}
