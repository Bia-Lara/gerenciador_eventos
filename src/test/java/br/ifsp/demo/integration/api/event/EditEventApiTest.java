package br.ifsp.demo.integration.api.event;

import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.dto.EditEventRequest;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Edição de evento (PUT /events/{eventId})")
class EditEventApiTest extends BaseApiTest {

    private static final String PASSWORD = "123456";
    private static final String EVENTS_PATH = BASE_PATH + "/events";

    private String novoUsuario(String email) {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(), email, PASSWORD);
    }

    @Test
    @DisplayName("Deve editar o evento, retornar 200 com o payload atualizado e persistir a alteração")
    void deveEditarEventoDoOrganizador() {
        String email = EntityBuilder.randomEmail();
        String token = novoUsuario(email);
        UUID organizerId = userRepository.findByEmail(email).orElseThrow().getId();
        UUID eventId = RegistrationApiFixture.createEvent(token, "Evento original",
                RegistrationApiFixture.futureStart());

        LocalDateTime novoInicio = RegistrationApiFixture.futureStart().plusDays(5);
        LocalDateTime novoFim = novoInicio.plusHours(4);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new EditEventRequest("  Evento atualizado  ", novoInicio, novoFim))
        .when()
                .put(EVENTS_PATH + "/" + eventId)
        .then()
                .statusCode(200)
                .body("id", equalTo(eventId.toString()))
                .body("name", equalTo("Evento atualizado"))
                .body("organizerId", equalTo(organizerId.toString()))
                .body("status", equalTo(EventStatus.ACTIVE.name()))
                .body("startDateTime", equalTo(novoInicio.toString()))
                .body("endDateTime", equalTo(novoFim.toString()));

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get(EVENTS_PATH + "/mine")
        .then()
                .statusCode(200)
                .body("[0].name", equalTo("Evento atualizado"))
                .body("[0].startDateTime", equalTo(novoInicio.toString()));
    }
}
