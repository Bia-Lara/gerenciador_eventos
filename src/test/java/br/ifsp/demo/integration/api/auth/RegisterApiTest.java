package br.ifsp.demo.integration.api.auth;

import br.ifsp.demo.infrastructure.security.auth.RegisterUserRequest;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
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

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com e-mail já registrado retorna 409 com corpo de erro descritivo")
    void cadastroComEmailJaRegistradoRetorna409() {
        RegisterUserRequest request = EntityBuilder.createRandomRegisterRequest("SenhaForte123");

        given().contentType("application/json").body(request)
                .when().post(REGISTER_PATH)
                .then().statusCode(201);

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(409)
                .body("status", equalTo("CONFLICT"))
                .body("message", notNullValue());
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com nome em branco deveria ser rejeitado com 400")
    void cadastroComNomeEmBrancoDeveriaSerRejeitado() {
        RegisterUserRequest request = EntityBuilder.createRegisterRequest(
                "", EntityBuilder.randomLastName(), EntityBuilder.randomEmail(), "SenhaForte123");

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com campo 'name' ausente no corpo deveria ser rejeitado com 400")
    void cadastroComCampoNomeAusenteDeveriaSerRejeitado() {
        given()
                .contentType("application/json")
                .body("""
                        {
                          "lastname": "%s",
                          "email": "%s",
                          "password": "SenhaForte123"
                        }
                        """.formatted(EntityBuilder.randomLastName(), EntityBuilder.randomEmail()))
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com e-mail em branco deveria ser rejeitado com 400")
    void cadastroComEmailEmBrancoDeveriaSerRejeitado() {
        RegisterUserRequest request = EntityBuilder.createRegisterRequest(
                EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(), "", "SenhaForte123");

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com senha em branco deveria ser rejeitado com 400")
    void cadastroComSenhaEmBrancoDeveriaSerRejeitado() {
        RegisterUserRequest request = EntityBuilder.createRegisterRequest(
                EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(), EntityBuilder.randomEmail(), "");

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com e-mail em formato inválido deveria ser rejeitado com 400")
    void cadastroComEmailEmFormatoInvalidoDeveriaSerRejeitado() {
        RegisterUserRequest request = EntityBuilder.createRegisterRequest(
                EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                "email-sem-arroba.com", "SenhaForte123");

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com e-mail já cadastrado, mas com tudo em maiúsuclo, deveria ser rejeitado com 409")
    void cadastroComEmailEmOutraCaixaDeveriaSerRejeitado() {
        String email = EntityBuilder.randomEmail();
        String lastname = EntityBuilder.randomLastName();

        RegisterUserRequest firstRequest = EntityBuilder.createRegisterRequest(
                EntityBuilder.randomFirstName(), lastname, email, "SenhaForte123");

        given()
                .contentType("application/json")
                .body(firstRequest)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(201);

        RegisterUserRequest secondRequest = EntityBuilder.createRegisterRequest(
                EntityBuilder.randomFirstName(), lastname, email.toUpperCase(), "OutraSenha456");

        given()
                .contentType("application/json")
                .body(secondRequest)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(409);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Corpo de requisição malformado retorna 400")
    void corpoMalformadoRetorna400() {
        given()
                .contentType("application/json")
                .body("{ isso não é um json válido")
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    @Tag("ApiTest")
    @Tag("IntegrationTest")
    @DisplayName("Cadastro com senha abaixo do tamanho mínimo deveria ser rejeitado com 400")
    void cadastroComSenhaAbaixoDoTamanhoMinimoDeveriaSerRejeitado() {
        RegisterUserRequest request = EntityBuilder.createRegisterRequest(
                EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), "1234567");

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(REGISTER_PATH)
                .then()
                .statusCode(400);
    }

}
