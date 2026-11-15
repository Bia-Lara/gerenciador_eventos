package br.ifsp.demo.integration.api.auth;

import br.ifsp.demo.infrastructure.security.auth.RegisterUserRequest;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

class RegisterApiTest extends BaseApiTest {

    private static final String REGISTER_PATH = BASE_PATH + "/register";

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com todos os campos válidos retorna 201 com o id do usuário")
    void cadastroComCamposValidosRetorna201ComId() {
        RegisterUserRequest request = EntityBuilder.createRandomRegisterRequest("SenhaForte123");

        given()
                .contentType("application/json")
                .body(request)
        .when()
                .post(REGISTER_PATH)
        .then()
                .statusCode(201)
                .body("id", notNullValue());
    }
}
