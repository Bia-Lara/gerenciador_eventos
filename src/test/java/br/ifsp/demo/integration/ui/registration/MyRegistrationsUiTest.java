package br.ifsp.demo.integration.ui.registration;

import br.ifsp.demo.support.BaseRegistrationsUiTest;
import br.ifsp.demo.support.RegistrationApiFixture;
import br.ifsp.demo.support.pages.MyRegistrationsPage;
import br.ifsp.demo.support.pages.MyRegistrationsPage.RegistrationCard;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MyRegistrationsUiTest extends BaseRegistrationsUiTest {

    private final Faker faker = new Faker();

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
}