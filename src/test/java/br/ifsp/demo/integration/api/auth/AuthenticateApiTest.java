package br.ifsp.demo.integration.api.auth;

import br.ifsp.demo.infrastructure.security.auth.AuthRequest;
import br.ifsp.demo.infrastructure.security.auth.RegisterUserRequest;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

class AuthenticateApiTest extends BaseApiTest {

    private static final String REGISTER_PATH = BASE_PATH + "/register";
    private static final String AUTHENTICATE_PATH = BASE_PATH + "/authenticate";

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Autenticação com credenciais válidas retorna 200 com token")
    void autenticacaoComCredenciaisValidasRetorna200ComToken() {
        RegisterUserRequest registerRequest = EntityBuilder.createRandomRegisterRequest("SenhaForte123");

        given().contentType("application/json").body(registerRequest)
                .when().post(REGISTER_PATH)
                .then().statusCode(201);

        AuthRequest authRequest = new AuthRequest(registerRequest.email(), "SenhaForte123");

        given()
                .contentType("application/json")
                .body(authRequest)
        .when()
                .post(AUTHENTICATE_PATH)
        .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

}
