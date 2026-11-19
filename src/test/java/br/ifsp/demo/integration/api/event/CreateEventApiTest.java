package br.ifsp.demo.integration.api.event;

import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Criação de evento (POST /events)")
class CreateEventApiTest extends BaseApiTest {

    private static final String PASSWORD = "123456";
    private static final String EVENTS_PATH = BASE_PATH + "/events";

    private static LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
    }

    private String novoOrganizador(String email) {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(), email, PASSWORD);
    }

    @Test
    @DisplayName("Deve criar evento válido e retornar 201 com o payload completo")
    void deveCriarEventoValido() {
        String email = EntityBuilder.randomEmail();
        String token = novoOrganizador(email);
        UUID organizerId = userRepository.findByEmail(email).orElseThrow().getId();
        LocalDateTime start = futureStart();
        LocalDateTime end = start.plusHours(3);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new CreateEventRequest("  Feira de ideias  ", start, end))
        .when()
                .post(EVENTS_PATH)
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Feira de ideias"))
                .body("organizerId", equalTo(organizerId.toString()))
                .body("status", equalTo(EventStatus.ACTIVE.name()))
                .body("categories", empty())
                .body("startDateTime", equalTo(start.toString()))
                .body("endDateTime", equalTo(end.toString()));
    }
}
