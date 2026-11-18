package br.ifsp.demo.integration.api.registration;

import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Consulta de inscricoes")
class QueryRegistrationApiTest extends BaseApiTest {

    private static final String PASSWORD = "123456";

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
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
}