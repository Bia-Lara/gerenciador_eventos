package br.ifsp.demo.integration.ui.event;

import br.ifsp.demo.support.BaseRegistrationsUiTest;
import br.ifsp.demo.support.RegistrationApiFixture;
import br.ifsp.demo.support.pages.EventDialog;
import br.ifsp.demo.support.pages.OrganizerPage;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;

@Tag("UiTest")
@DisplayName("UI - Painel Organizar: eventos")
class OrganizerEventsUiTest extends BaseRegistrationsUiTest {

    private final Faker faker = new Faker();

    private String token;
    private OrganizerPage painel;

    @BeforeEach
    void entrarComoOrganizador() {
        token = novoUsuario();
        entrarComToken(token);
        painel = new OrganizerPage(driver, wait);
    }

    private String nomeUnico() {
        return faker.lorem().word() + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Criar evento válido o mostra na lista, atualiza o contador e o grava no servidor")
    void criarEventoValidoMostraNaListaEGravaNoServidor() {
        String nome = nomeUnico();
        LocalDateTime inicio = RegistrationApiFixture.futureStart();

        EventDialog dialogo = painel.abrirNovoEvento();
        dialogo.preencher(nome, inicio, inicio.plusHours(2)).salvar();

        assertThat(painel.aguardarMensagemDeFeedback()).isEqualTo("Evento criado.");
        assertThat(painel.nomesDosEventos()).containsExactly(nome);
        assertThat(painel.totalDeEventos()).isEqualTo("1");
        given().header("Authorization", "Bearer " + token)
                .when().get(BASE_PATH + "/events/mine")
                .then().statusCode(200).body("name", hasItem(nome));
    }
}
