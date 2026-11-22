package br.ifsp.demo.integration.ui.category;

import br.ifsp.demo.dto.CategoryResponse;
import br.ifsp.demo.support.BaseRegistrationsUiTest;
import br.ifsp.demo.support.RegistrationApiFixture;
import br.ifsp.demo.support.pages.CategorySection;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
@DisplayName("UI - Seção de categorias do evento")
class CategorySectionUiTest extends BaseRegistrationsUiTest {

    private final Faker faker = new Faker();

    private String organizerToken;
    private UUID eventId;
    private CategorySection secao;

    @BeforeEach
    void entrarComoOrganizadorDeUmEvento() {
        organizerToken = novoUsuario();
        String nomeDoEvento = "Encontro " + faker.lorem().word() + " " + UUID.randomUUID().toString().substring(0, 8);
        eventId = RegistrationApiFixture.createEvent(organizerToken, nomeDoEvento, RegistrationApiFixture.futureStart());
        entrarComToken(organizerToken);
        secao = new CategorySection(driver, wait, nomeDoEvento);
    }

    @Test
    @DisplayName("Adicionar categoria válida a mostra na lista e no contador e a vincula ao evento")
    void adicionarCategoriaValidaMostraNaListaEVinculaAoEvento() {
        String nome = faker.commerce().department() + " " + faker.number().digits(3);
        int capacidade = faker.number().numberBetween(1, 200);

        secao.adicionar(nome, String.valueOf(capacidade), "75.50");

        assertThat(secao.aguardarMensagemDeFeedback()).isEqualTo("Categoria adicionada.");
        List<String> categorias = secao.categoriasListadas();
        assertThat(categorias).hasSize(1);
        assertThat(categorias.get(0)).startsWith(nome + " · " + capacidade + " vagas").contains("75,50");
        assertThat(secao.contador()).isEqualTo("(1)");
        assertThat(RegistrationApiFixture.listCategories(organizerToken, eventId))
                .extracting(CategoryResponse::name)
                .containsExactly(nome);
    }
}
