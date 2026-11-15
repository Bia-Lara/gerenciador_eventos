package br.ifsp.demo.support;

import br.ifsp.demo.infrastructure.security.auth.AuthRequest;
import br.ifsp.demo.infrastructure.security.auth.RegisterUserRequest;
import br.ifsp.demo.infrastructure.security.user.JpaUserRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseApiTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected JpaUserRepository userRepository;

    protected static final String BASE_PATH = "/api/v1";

    @BeforeEach
    void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    protected String registerAndAuthenticate(String name, String lastname, String email, String password) {
        given()
                .contentType("application/json")
                .body(new RegisterUserRequest(name, lastname, email, password))
        .when()
                .post(BASE_PATH + "/register")
        .then()
                .statusCode(201);

        return authenticate(email, password);
    }

    protected String authenticate(String username, String password) {
        return given()
                .contentType("application/json")
                .body(new AuthRequest(username, password))
        .when()
                .post(BASE_PATH + "/authenticate")
        .then()
                .statusCode(200)
                .extract()
                .path("token");
    }
}
