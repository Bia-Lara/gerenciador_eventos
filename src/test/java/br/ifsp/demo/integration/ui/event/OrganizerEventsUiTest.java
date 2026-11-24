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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

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

    @Test
    @DisplayName("Nome vazio é barrado pelo próprio formulário, sem criar o evento")
    void nomeVazioEBarradoPeloFormulario() {
        LocalDateTime inicio = RegistrationApiFixture.futureStart();

        EventDialog dialogo = painel.abrirNovoEvento();
        dialogo.preencher("", inicio, inicio.plusHours(2)).salvar();

        assertThat(dialogo.nomeAceitoPeloNavegador()).isFalse();
        assertThat(dialogo.estaAberto()).isTrue();
        assertThat(painel.nomesDosEventos()).isEmpty();
        given().header("Authorization", "Bearer " + token)
                .when().get(BASE_PATH + "/events/mine")
                .then().statusCode(200).body("$", empty());
    }

    @Test
    @DisplayName("Término antes do início mostra o erro no diálogo e não cria o evento")
    void terminoAntesDoInicioMostraErroNoDialogo() {
        String nome = nomeUnico();
        LocalDateTime inicio = RegistrationApiFixture.futureStart();

        EventDialog dialogo = painel.abrirNovoEvento();
        dialogo.preencher(nome, inicio, inicio.minusHours(1)).salvar();

        assertThat(dialogo.aguardarMensagemDeErro()).isNotBlank();
        assertThat(dialogo.estaAberto()).isTrue();
        given().header("Authorization", "Bearer " + token)
                .when().get(BASE_PATH + "/events/mine")
                .then().statusCode(200).body("$", empty());
    }

    @Test
    @DisplayName("Editar evento abre o diálogo preenchido e a alteração aparece na lista e no servidor")
    void editarEventoAtualizaListaEServidor() {
        String nomeOriginal = nomeUnico();
        String nomeNovo = nomeUnico();
        LocalDateTime inicio = RegistrationApiFixture.futureStart();
        RegistrationApiFixture.createEvent(token, nomeOriginal, inicio);
        driver.navigate().refresh();

        EventDialog dialogo = painel.abrirEdicao(nomeOriginal);
        assertThat(dialogo.titulo()).isEqualTo("Editar evento");
        assertThat(dialogo.nome()).isEqualTo(nomeOriginal);
        dialogo.preencher(nomeNovo, inicio.plusDays(1), inicio.plusDays(1).plusHours(2)).salvar();

        assertThat(painel.aguardarMensagemDeFeedback()).isEqualTo("Evento atualizado.");
        assertThat(painel.nomesDosEventos()).containsExactly(nomeNovo);
        given().header("Authorization", "Bearer " + token)
                .when().get(BASE_PATH + "/events/mine")
                .then().statusCode(200)
                .body("name", hasItem(nomeNovo))
                .body("name", not(hasItem(nomeOriginal)));
    }

    @Test
    @DisplayName("Cancelar evento confirmando marca o card como cancelado e atualiza o contador")
    void cancelarEventoConfirmandoMarcaComoCancelado() {
        String nome = nomeUnico();
        UUID eventId = RegistrationApiFixture.createEvent(token, nome, RegistrationApiFixture.futureStart());
        driver.navigate().refresh();

        String pergunta = painel.cancelar(nome, true);

        assertThat(pergunta).isEqualTo("Cancelar o evento \"" + nome + "\"?");
        assertThat(painel.aguardarMensagemDeFeedback("Evento cancelado.")).isEqualTo("Evento cancelado.");
        assertThat(painel.statusDoEvento(nome)).isEqualTo("CANCELADO");
        assertThat(painel.totalDeCancelados()).isEqualTo("1");
        given().header("Authorization", "Bearer " + token)
                .when().get(BASE_PATH + "/events/mine")
                .then().statusCode(200)
                .body("find { it.id == '" + eventId + "' }.status", equalTo("CANCELLED"));
    }
}
