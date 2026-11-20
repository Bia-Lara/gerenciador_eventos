package br.ifsp.demo.integration.ui.registration;

import br.ifsp.demo.support.BaseRegistrationsUiTest;
import br.ifsp.demo.support.RegistrationApiFixture;
import br.ifsp.demo.support.pages.MyRegistrationsPage;
import br.ifsp.demo.support.pages.MyRegistrationsPage.RegistrationCard;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyRegistrationsUiTest extends BaseRegistrationsUiTest {

    private final Faker faker = new Faker();

    private String nomeUnicoDeEvento() {
        return "Encontro " + faker.lorem().word() + " " + UUID.randomUUID().toString().substring(0, 8);
    }

    private UUID inscreverEmEvento(String organizador, String participante, String nomeDoEvento, LocalDateTime inicio) {
        UUID eventId = RegistrationApiFixture.createEvent(organizador, nomeDoEvento, inicio);
        UUID categoryId = RegistrationApiFixture.createCategory(organizador, eventId, "Geral", 50.0, 40);
        return RegistrationApiFixture.register(participante, eventId, categoryId);
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Minhas inscrições sem nenhuma inscrição exibe o estado vazio")
    void minhasInscricoesSemInscricoesExibeEstadoVazio() {
        entrarComToken(novoUsuario());
        MyRegistrationsPage page = new MyRegistrationsPage(driver, wait).abrir();

        assertThat(page.aguardarEstadoVazio()).isTrue();
        assertThat(page.textoDoEstadoVazio()).contains("Nenhuma inscrição por aqui.");
        assertThat(page.quantidadeDeCards()).isZero();
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Card da inscrição ativa exibe evento, horário, categoria, status e botão de cancelar")
    void cardDaInscricaoAtivaExibeDadosDoEvento() {
        String organizador = novoUsuario();
        String participante = novoUsuario();
        String nomeDoEvento = "Encontro " + faker.lorem().word() + " " + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime inicio = RegistrationApiFixture.futureStart();
        UUID eventId = RegistrationApiFixture.createEvent(organizador, nomeDoEvento, inicio);
        UUID categoryId = RegistrationApiFixture.createCategory(organizador, eventId, "Geral", 75.5, 40);
        RegistrationApiFixture.register(participante, eventId, categoryId);

        entrarComToken(participante);
        RegistrationCard card = new MyRegistrationsPage(driver, wait).abrir().cardDoEvento(nomeDoEvento);

        DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm");
        assertThat(card.detalhes()).contains(hora.format(inicio) + " às " + hora.format(inicio.plusHours(3)));
        assertThat(card.detalhes()).contains("Inscrição 1 de 1");
        assertThat(card.categoria()).contains("Geral").contains("75,50");
        assertThat(card.status()).isEqualTo("ATIVA");
        assertThat(card.possuiBotaoCancelar()).isTrue();
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Cards aparecem ordenados pela data de início do evento")
    void cardsAparecemOrdenadosPelaDataDeInicio() {
        String organizador = novoUsuario();
        String participante = novoUsuario();
        String eventoTarde = nomeUnicoDeEvento();
        String eventoCedo = nomeUnicoDeEvento();
        inscreverEmEvento(organizador, participante, eventoTarde, RegistrationApiFixture.futureStart().plusDays(10));
        inscreverEmEvento(organizador, participante, eventoCedo, RegistrationApiFixture.futureStart().minusDays(5));

        entrarComToken(participante);
        List<String> nomes = new MyRegistrationsPage(driver, wait).abrir().nomesDosEventosNaOrdemExibida();

        assertThat(nomes).containsExactly(eventoCedo, eventoTarde);
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Duas inscrições no mesmo evento mostram Inscrição 1 de 2 e Inscrição 2 de 2")
    void duasInscricoesNoMesmoEventoMostramPosicaoDeCadaUma() {
        String organizador = novoUsuario();
        String participante = novoUsuario();
        String nomeDoEvento = nomeUnicoDeEvento();
        UUID eventId = RegistrationApiFixture.createEvent(organizador, nomeDoEvento, RegistrationApiFixture.futureStart());
        UUID categoryId = RegistrationApiFixture.createCategory(organizador, eventId, "Geral", 50.0, 40);
        UUID primeira = RegistrationApiFixture.register(participante, eventId, categoryId);
        RegistrationApiFixture.cancel(participante, primeira);
        RegistrationApiFixture.register(participante, eventId, categoryId);

        entrarComToken(participante);
        List<String> detalhes = new MyRegistrationsPage(driver, wait).abrir().detalhesDosCardsDoEvento(nomeDoEvento);

        assertThat(detalhes).hasSize(2);
        assertThat(detalhes.get(0)).contains("Inscrição 1 de 2");
        assertThat(detalhes.get(1)).contains("Inscrição 2 de 2");
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Minhas inscrições não exibe inscrições de outro usuário")
    void minhasInscricoesNaoExibeInscricoesDeOutroUsuario() {
        String organizador = novoUsuario();
        String participanteA = novoUsuario();
        String participanteB = novoUsuario();
        String eventoDeA = nomeUnicoDeEvento();
        String eventoDeB = nomeUnicoDeEvento();
        inscreverEmEvento(organizador, participanteA, eventoDeA, RegistrationApiFixture.futureStart());
        inscreverEmEvento(organizador, participanteB, eventoDeB, RegistrationApiFixture.futureStart());

        entrarComToken(participanteA);
        List<String> nomes = new MyRegistrationsPage(driver, wait).abrir().nomesDosEventosNaOrdemExibida();

        assertThat(nomes).containsExactly(eventoDeA);
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Filtros Todas, Ativas e Encerradas mostram as inscrições correspondentes")
    void filtrosMostramAsInscricoesCorrespondentes() {
        String organizador = novoUsuario();
        String participante = novoUsuario();
        String eventoAtivo = nomeUnicoDeEvento();
        String eventoCancelado = nomeUnicoDeEvento();
        inscreverEmEvento(organizador, participante, eventoAtivo, RegistrationApiFixture.futureStart());
        UUID inscricaoCancelada = inscreverEmEvento(organizador, participante, eventoCancelado, RegistrationApiFixture.futureStart());
        RegistrationApiFixture.cancel(participante, inscricaoCancelada);

        entrarComToken(participante);
        MyRegistrationsPage page = new MyRegistrationsPage(driver, wait).abrir();

        assertThat(page.aguardarNomesNaTela(eventoAtivo, eventoCancelado))
                .containsExactlyInAnyOrder(eventoAtivo, eventoCancelado);
        assertThat(page.filtrarPor("Ativas").aguardarNomesNaTela(eventoAtivo))
                .containsExactly(eventoAtivo);
        assertThat(page.filtrarPor("Encerradas").aguardarNomesNaTela(eventoCancelado))
                .containsExactly(eventoCancelado);
        assertThat(page.filtrarPor("Todas").aguardarNomesNaTela(eventoAtivo, eventoCancelado))
                .containsExactlyInAnyOrder(eventoAtivo, eventoCancelado);
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Cancelar a inscrição confirmando exibe mensagem, marca como cancelada e remove o botão")
    void cancelarInscricaoConfirmandoMarcaComoCancelada() {
        String organizador = novoUsuario();
        String participante = novoUsuario();
        String nomeDoEvento = nomeUnicoDeEvento();
        inscreverEmEvento(organizador, participante, nomeDoEvento, RegistrationApiFixture.futureStart());

        entrarComToken(participante);
        MyRegistrationsPage page = new MyRegistrationsPage(driver, wait).abrir();
        page.cancelarInscricaoDoEvento(nomeDoEvento, true);

        assertThat(page.aguardarMensagemDeFeedback()).isEqualTo("Inscrição cancelada.");
        RegistrationCard card = page.cardDoEvento(nomeDoEvento);
        assertThat(card.status()).isEqualTo("CANCELADA");
        assertThat(card.possuiBotaoCancelar()).isFalse();
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Recusar a confirmação não cancela a inscrição")
    void recusarConfirmacaoNaoCancelaInscricao() {
        String organizador = novoUsuario();
        String participante = novoUsuario();
        String nomeDoEvento = nomeUnicoDeEvento();
        inscreverEmEvento(organizador, participante, nomeDoEvento, RegistrationApiFixture.futureStart());

        entrarComToken(participante);
        MyRegistrationsPage page = new MyRegistrationsPage(driver, wait).abrir();
        page.cancelarInscricaoDoEvento(nomeDoEvento, false);

        RegistrationCard card = page.cardDoEvento(nomeDoEvento);
        assertThat(card.status()).isEqualTo("ATIVA");
        assertThat(card.possuiBotaoCancelar()).isTrue();
        List<String> statusNoSistema = given()
                .header("Authorization", "Bearer " + participante)
                .when()
                .get("/api/v1/registrations/details")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("status", String.class);
        assertThat(statusNoSistema).containsExactly("ATIVA");
    }
}