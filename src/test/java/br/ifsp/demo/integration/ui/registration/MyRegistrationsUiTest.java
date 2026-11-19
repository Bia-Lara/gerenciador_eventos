package br.ifsp.demo.integration.ui.registration;

import br.ifsp.demo.support.BaseRegistrationsUiTest;
import br.ifsp.demo.support.pages.MyRegistrationsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyRegistrationsUiTest extends BaseRegistrationsUiTest {

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
}