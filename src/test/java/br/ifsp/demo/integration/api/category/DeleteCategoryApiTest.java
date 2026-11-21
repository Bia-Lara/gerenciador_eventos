package br.ifsp.demo.integration.api.category;

import br.ifsp.demo.dto.CategoryResponse;
import br.ifsp.demo.support.BaseApiTest;
import br.ifsp.demo.support.EntityBuilder;
import br.ifsp.demo.support.RegistrationApiFixture;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("API - Remoção de categoria")
class DeleteCategoryApiTest extends BaseApiTest {

    private static final String PASSWORD = "SenhaForte123";

    private String organizerToken;
    private UUID eventId;
    private CategoryResponse categoria;

    @BeforeEach
    void prepararEventoComCategoria() {
        organizerToken = registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
        eventId = RegistrationApiFixture.createEvent(organizerToken,
                "Evento " + UUID.randomUUID(), RegistrationApiFixture.futureStart());
        UUID categoryId = RegistrationApiFixture.createCategory(organizerToken, eventId, "Inteira", 50.0, 10);
        categoria = new CategoryResponse(categoryId, "Inteira", 10, 50.0);
    }

    private String novoUsuario() {
        return registerAndAuthenticate(EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), PASSWORD);
    }

    private Response removerCategoria(String token, UUID evento, Object categoryId) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(BASE_PATH + "/events/" + evento + "/categories/" + categoryId);
    }

    private List<CategoryResponse> categoriasDoEvento(UUID evento) {
        return RegistrationApiFixture.listCategories(organizerToken, evento);
    }

    @Test
    @DisplayName("Deve remover categoria sem inscrições, retornar 204 e desvinculá-la do evento")
    void deveRemoverCategoriaSemInscricoes() {
        removerCategoria(organizerToken, eventId, categoria.id())
                .then()
                .statusCode(204)
                .body(emptyOrNullString());

        assertThat(categoriasDoEvento(eventId)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar 403 ao remover categoria com inscrição ativa, mantendo-a no evento")
    void deveRetornar403ComInscricaoAtiva() {
        RegistrationApiFixture.register(novoUsuario(), eventId, categoria.id());

        removerCategoria(organizerToken, eventId, categoria.id())
                .then()
                .statusCode(403)
                .body("message", equalTo("Category has registrations"));

        assertThat(categoriasDoEvento(eventId)).containsExactly(categoria);
    }

    @Test
    @DisplayName("Deve remover categoria cujas inscrições foram todas canceladas e retornar 204")
    void deveRemoverCategoriaComInscricoesSomenteCanceladas() {
        String participanteToken = novoUsuario();
        UUID inscricao = RegistrationApiFixture.register(participanteToken, eventId, categoria.id());
        RegistrationApiFixture.cancel(participanteToken, inscricao);

        removerCategoria(organizerToken, eventId, categoria.id())
                .then()
                .statusCode(204);

        assertThat(categoriasDoEvento(eventId)).isEmpty();
    }
}
