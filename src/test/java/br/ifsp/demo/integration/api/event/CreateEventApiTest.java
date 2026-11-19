package br.ifsp.demo.integration.api.event;

import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Stream;

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

    @Test
    @DisplayName("Deve aceitar nome com exatamente 150 caracteres (limite)")
    void deveAceitarNomeNoLimiteDe150Caracteres() {
        String token = novoOrganizador(EntityBuilder.randomEmail());
        LocalDateTime start = futureStart();
        String nomeNoLimite = "a".repeat(150);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(new CreateEventRequest(nomeNoLimite, start, start.plusHours(1)))
        .when()
                .post(EVENTS_PATH)
        .then()
                .statusCode(201)
                .body("name", equalTo(nomeNoLimite));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestsEventosInvalidos")
    @DisplayName("Deve retornar 400 para dados de evento inválidos")
    void deveRetornar400ParaDadosInvalidos(String caso, CreateEventRequest request) {
        String token = novoOrganizador(EntityBuilder.randomEmail());

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(request)
        .when()
                .post(EVENTS_PATH)
        .then()
                .statusCode(400);
    }

    private static Stream<Arguments> requestsEventosInvalidos() {
        LocalDateTime start = futureStart();
        return Stream.of(
                Arguments.of("nome nulo", new CreateEventRequest(null, start, start.plusHours(2))),
                Arguments.of("nome vazio", new CreateEventRequest("", start, start.plusHours(2))),
                Arguments.of("nome só com espaços", new CreateEventRequest("   ", start, start.plusHours(2))),
                Arguments.of("nome com 151 caracteres", new CreateEventRequest("a".repeat(151), start, start.plusHours(2))),
                Arguments.of("início ausente", new CreateEventRequest("Evento", null, start.plusHours(2))),
                Arguments.of("término ausente", new CreateEventRequest("Evento", start, null)),
                Arguments.of("término igual ao início", new CreateEventRequest("Evento", start, start)),
                Arguments.of("término antes do início", new CreateEventRequest("Evento", start, start.minusHours(1))),
                Arguments.of("início no passado",
                        new CreateEventRequest("Evento", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(1)))
        );
    }

    @Test
    @DisplayName("Deve retornar 401 quando não houver token")
    void deveRetornar401SemToken() {
        LocalDateTime start = futureStart();

        given()
                .contentType("application/json")
                .body(new CreateEventRequest("Evento", start, start.plusHours(1)))
        .when()
                .post(EVENTS_PATH)
        .then()
                .statusCode(401);
    }
}
