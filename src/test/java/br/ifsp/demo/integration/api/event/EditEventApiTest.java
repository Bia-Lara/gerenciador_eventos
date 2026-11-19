package br.ifsp.demo.integration.api.event;

import br.ifsp.demo.domain.enumerations.EventStatus;
import br.ifsp.demo.dto.EditEventRequest;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("requestsEdicaoInvalidos")
    @DisplayName("Deve retornar 400 para dados de edição inválidos")
    void deveRetornar400ParaDadosInvalidos(String caso, EditEventRequest request) {
        String token = novoUsuario(EntityBuilder.randomEmail());
        UUID eventId = RegistrationApiFixture.createEvent(token, "Evento original",
                RegistrationApiFixture.futureStart());

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(request)
        .when()
                .put(EVENTS_PATH + "/" + eventId)
        .then()
                .statusCode(400);
    }

    private static Stream<Arguments> requestsEdicaoInvalidos() {
        LocalDateTime start = RegistrationApiFixture.futureStart().plusDays(5);
        return Stream.of(
                Arguments.of("nome nulo", new EditEventRequest(null, start, start.plusHours(2))),
                Arguments.of("nome vazio", new EditEventRequest("", start, start.plusHours(2))),
                Arguments.of("nome só com espaços", new EditEventRequest("   ", start, start.plusHours(2))),
                Arguments.of("nome com 151 caracteres", new EditEventRequest("a".repeat(151), start, start.plusHours(2))),
                Arguments.of("início ausente", new EditEventRequest("Evento", null, start.plusHours(2))),
                Arguments.of("término ausente", new EditEventRequest("Evento", start, null)),
                Arguments.of("término igual ao início", new EditEventRequest("Evento", start, start)),
                Arguments.of("término antes do início", new EditEventRequest("Evento", start, start.minusHours(1))),
                Arguments.of("início no passado",
                        new EditEventRequest("Evento", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(1)))
        );
    }

    @Test
    @DisplayName("Deve retornar 403 quando quem edita não é o organizador do evento")
    void deveRetornar403QuandoNaoForOrganizador() {
        String tokenOrganizador = novoUsuario(EntityBuilder.randomEmail());
        String tokenOutroUsuario = novoUsuario(EntityBuilder.randomEmail());
        UUID eventId = RegistrationApiFixture.createEvent(tokenOrganizador, "Evento original",
                RegistrationApiFixture.futureStart());
        LocalDateTime novoInicio = RegistrationApiFixture.futureStart().plusDays(5);

        given()
                .header("Authorization", "Bearer " + tokenOutroUsuario)
                .contentType("application/json")
                .body(new EditEventRequest("Tentativa de edição", novoInicio, novoInicio.plusHours(2)))
                .when()
                .put(EVENTS_PATH + "/" + eventId)
                .then()
                .statusCode(403);
    }
}
