package br.ifsp.demo.support;

import br.ifsp.demo.dto.CreateEventRequest;
import br.ifsp.demo.dto.CreateCategoryRequest;
import br.ifsp.demo.dto.RegistrationBody;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import static io.restassured.RestAssured.given;

public class RegistrationApiFixture {

    private RegistrationApiFixture() {}

    public static LocalDateTime futureStart() {
        return LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.SECONDS);
    }

    public static UUID createEvent(String organizerToken, String name, LocalDateTime start) {
        String id = given()
                .header("Authorization", "Bearer " + organizerToken)
                .contentType("application/json")
                .body(new CreateEventRequest(name, start, start.plusHours(3)))
                .when()
                .post("/api/v1/events")
                .then()
                .statusCode(201)
                .extract().path("id");
        return UUID.fromString(id);
    }

    public static UUID createCategory(String organizerToken, UUID eventId, String name, double price, int capacity) {
        String id = given()
                .header("Authorization", "Bearer " + organizerToken)
                .contentType("application/json")
                .body(new CreateCategoryRequest(name, price, capacity))
                .when()
                .post("/api/v1/events/" + eventId + "/categories")
                .then()
                .statusCode(201)
                .extract().path("id");
        return UUID.fromString(id);
    }

    public static UUID register(String participantToken, UUID eventId, UUID categoryId) {
        String id = given()
                .header("Authorization", "Bearer " + participantToken)
                .contentType("application/json")
                .body(new RegistrationBody(categoryId))
                .when()
                .post("/api/v1/events/" + eventId + "/registrations")
                .then()
                .statusCode(201)
                .extract().path("id");
        return UUID.fromString(id);
    }

    public static void cancel(String participantToken, UUID registrationId) {
        given()
                .header("Authorization", "Bearer " + participantToken)
                .contentType("application/json")
                .when()
                .post("/api/v1/events/registrations/" + registrationId + "/cancel")
                .then()
                .statusCode(204);
    }
}