package br.ifsp.demo.integration.api.category;

import br.ifsp.demo.dto.CategoryResponse;
import br.ifsp.demo.dto.CreateCategoryRequest;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Criação de categoria")
class CreateCategoryApiTest extends BaseApiTest {

    private static final String PASSWORD = "SenhaForte123";

    private String organizerToken;
    private UUID eventId;

    @BeforeEach
    void prepararEventoDoOrganizador() {
        organizerToken = novoUsuario();
        eventId = RegistrationApiFixture.createEvent(organizerToken,
                "Evento " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
    }

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
    }

    private Response criarCategoria(String token, UUID evento, Object corpo) {
        return given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(corpo)
                .when()
                .post(BASE_PATH + "/events/" + evento + "/categories");
    }

    private List<CategoryResponse> categoriasDoEvento(UUID evento) {
        return given()
                .header("Authorization", "Bearer " + organizerToken)
                .when()
                .get(BASE_PATH + "/events/mine")
                .then()
                .statusCode(200)
                .extract().jsonPath()
                .param("eventId", evento.toString())
                .getList("find { it.id == eventId }.categories", CategoryResponse.class);
    }

    @Test
    @DisplayName("Deve criar categoria válida, retornar 201 com o payload e vinculá-la ao evento")
    void deveCriarCategoriaValida() {
        CategoryResponse criada = criarCategoria(organizerToken, eventId, new CreateCategoryRequest("Inteira", 75.5, 40))
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Inteira"))
                .body("capacity", equalTo(40))
                .body("price", equalTo(75.5f))
                .extract().as(CategoryResponse.class);

        assertThat(categoriasDoEvento(eventId)).containsExactly(criada);
    }

    @ParameterizedTest(name = "nome repetido \"{0}\"")
    @ValueSource(strings = {"Inteira", "  Inteira  "})
    @DisplayName("Deve retornar 409 ao repetir o nome de uma categoria do mesmo evento, sem criar outra")
    void deveRetornar409ComNomeDuplicado(String nomeRepetido) {
        CategoryResponse original = criarCategoria(organizerToken, eventId, new CreateCategoryRequest("Inteira", 75.5, 40))
                .then()
                .statusCode(201)
                .extract().as(CategoryResponse.class);

        criarCategoria(organizerToken, eventId, new CreateCategoryRequest(nomeRepetido, 10.0, 5))
                .then()
                .statusCode(409)
                .body("message", equalTo("Category name already exists"));

        assertThat(categoriasDoEvento(eventId)).containsExactly(original);
    }

    @Test
    @DisplayName("Deve aceitar nome de categoria com exatamente 150 caracteres")
    void deveAceitarNomeNoLimiteDe150Caracteres() {
        String nomeNoLimite = "a".repeat(150);

        CategoryResponse criada = criarCategoria(organizerToken, eventId, new CreateCategoryRequest(nomeNoLimite, 50.0, 10))
                .then()
                .statusCode(201)
                .body("name", equalTo(nomeNoLimite))
                .extract().as(CategoryResponse.class);

        assertThat(categoriasDoEvento(eventId)).containsExactly(criada);
    }

    @Test
    @DisplayName("Deve retornar 400 com nome de categoria acima de 150 caracteres, sem criá-la")
    void deveRetornar400ComNomeAcimaDe150Caracteres() {
        criarCategoria(organizerToken, eventId, new CreateCategoryRequest("a".repeat(151), 50.0, 10))
                .then()
                .statusCode(400)
                .body("message", equalTo("Category name must not exceed 150 characters"));

        assertThat(categoriasDoEvento(eventId)).isEmpty();
    }

    @ParameterizedTest(name = "Categoria com capacidade inválida: {0}")
    @CsvSource({
            "0, Category capacity must be greater than zero",
            "-1, Category capacity must not be negative",
            ", Category capacity is required"
    })
    @DisplayName("Deve retornar 400 com capacidade inválida, sem criar a categoria")
    void deveRetornar400ComCapacidadeInvalida(Integer capacidade, String mensagemEsperada) {
        criarCategoria(organizerToken, eventId, new CreateCategoryRequest("Inteira", 50.0, capacidade))
                .then()
                .statusCode(400)
                .body("message", equalTo(mensagemEsperada));

        assertThat(categoriasDoEvento(eventId)).isEmpty();
    }
}
