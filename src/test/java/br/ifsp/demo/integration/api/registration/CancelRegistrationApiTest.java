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
}