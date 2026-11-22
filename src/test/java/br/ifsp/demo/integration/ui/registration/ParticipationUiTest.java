package br.ifsp.demo.integration.ui.registration;

import br.ifsp.demo.support.BaseRegistrationsUiTest;
import br.ifsp.demo.support.RegistrationApiFixture;
import br.ifsp.demo.support.pages.ParticipationPage;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
@DisplayName("UI - Painel Inscrever-se e modal de inscrição")
class ParticipationUiTest extends BaseRegistrationsUiTest {

    private final Faker faker = new Faker();

    private String organizerToken;
    private String participantToken;
    private String nomeDoEvento;
    private LocalDateTime inicio;
    private UUID eventId;

    @BeforeEach
    void prepararEventoDeOutroOrganizador() {
        organizerToken = novoUsuario();
        participantToken = novoUsuario();
        nomeDoEvento = "Encontro " + faker.lorem().word() + " " + UUID.randomUUID().toString().substring(0, 8);
        inicio = RegistrationApiFixture.futureStart();
        eventId = RegistrationApiFixture.createEvent(organizerToken, nomeDoEvento, inicio);
    }

    private ParticipationPage abrirPainelComoParticipante() {
        entrarComToken(participantToken);
        return new ParticipationPage(driver, wait).abrir();
    }

    @Test
    @DisplayName("Evento de outro organizador aparece com as vagas preenchidas e o botão de inscrição")
    void eventoDeOutroOrganizadorApareceComVagas() {
        RegistrationApiFixture.createCategory(organizerToken, eventId, "Inteira", 50.0, 10);

        ParticipationPage painel = abrirPainelComoParticipante();

        assertThat(painel.vagasPreenchidasDoEvento(nomeDoEvento)).isEqualTo("0/10");
        assertThat(painel.eventoEstaLotado(nomeDoEvento)).isFalse();
        assertThat(painel.botaoDoEvento(nomeDoEvento).getText()).isEqualTo("Inscrever-se");
        assertThat(painel.botaoDoEvento(nomeDoEvento).isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Evento lotado aparece como LOTADO e com o botão de inscrição desabilitado")
    void eventoLotadoApareceComBotaoDesabilitado() {
        UUID categoryId = RegistrationApiFixture.createCategory(organizerToken, eventId, "Vaga única", 30.0, 1);
        RegistrationApiFixture.register(novoUsuario(), eventId, categoryId);

        ParticipationPage painel = abrirPainelComoParticipante();

        assertThat(painel.vagasPreenchidasDoEvento(nomeDoEvento)).isEqualTo("1/1");
        assertThat(painel.eventoEstaLotado(nomeDoEvento)).isTrue();
        assertThat(painel.botaoDoEvento(nomeDoEvento).getText()).isEqualTo("Lotado");
        assertThat(painel.botaoDoEvento(nomeDoEvento).isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Filtrar por uma data sem eventos mostra o aviso de que não há eventos nessa data")
    void filtrarPorDataSemEventosMostraAviso() {
        RegistrationApiFixture.createCategory(organizerToken, eventId, "Inteira", 50.0, 10);
        ParticipationPage painel = abrirPainelComoParticipante();
        assertThat(painel.vagasPreenchidasDoEvento(nomeDoEvento)).isEqualTo("0/10");

        painel.filtrarPorData(inicio.toLocalDate().plusYears(5));

        assertThat(painel.aguardarTituloDoEstadoVazio()).isEqualTo("Nenhum evento nesta data.");
        assertThat(painel.eventoEstaListado(nomeDoEvento)).isFalse();
    }
}
