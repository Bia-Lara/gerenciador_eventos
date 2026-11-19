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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Listagem dos meus eventos (GET /events/mine)")
class ListMyEventsApiTest extends BaseApiTest {

    private static final String PASSWORD = "123456";
    private static final String MY_EVENTS_PATH = BASE_PATH + "/events/mine";

    private String novoUsuario(String email) {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(), email, PASSWORD);
    }

    @Test
    @DisplayName("Deve listar apenas os eventos do organizador autenticado")
    void deveListarApenasOsEventosDoOrganizador() {
        String emailOrganizador = EntityBuilder.randomEmail();
        String tokenOrganizador = novoUsuario(emailOrganizador);
        UUID organizerId = userRepository.findByEmail(emailOrganizador).orElseThrow().getId();
        String tokenOutroOrganizador = novoUsuario(EntityBuilder.randomEmail());

        String nomeDoEvento = "Evento " + UUID.randomUUID();
        UUID eventId = RegistrationApiFixture.createEvent(tokenOrganizador, nomeDoEvento,
                RegistrationApiFixture.futureStart());
        RegistrationApiFixture.createEvent(tokenOutroOrganizador, "Evento " + UUID.randomUUID(),
                RegistrationApiFixture.futureStart());

        given()
                .header("Authorization", "Bearer " + tokenOrganizador)
        .when()
                .get(MY_EVENTS_PATH)
        .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(eventId.toString()))
                .body("[0].name", equalTo(nomeDoEvento))
                .body("[0].organizerId", equalTo(organizerId.toString()))
                .body("[0].status", equalTo(EventStatus.ACTIVE.name()))
                .body("[0].categories", empty());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando o organizador não criou eventos")
    void deveRetornarListaVaziaSemEventos() {
        String token = novoUsuario(EntityBuilder.randomEmail());

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get(MY_EVENTS_PATH)
        .then()
                .statusCode(200)
                .body("$", empty());
    }
}
