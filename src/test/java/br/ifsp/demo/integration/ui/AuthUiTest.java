package br.ifsp.demo.integration.ui;

import br.ifsp.demo.infrastructure.security.user.JpaUserRepository;
import br.ifsp.demo.infrastructure.security.user.User;
import br.ifsp.demo.support.AuthPageObject;
import br.ifsp.demo.support.BaseSeleniumTest;
import br.ifsp.demo.support.EntityBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class AuthUiTest extends BaseSeleniumTest {

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void limparBanco() {
        userRepository.deleteAll();
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Login com credenciais válidas deve levar ao dashboard")
    void loginComCredenciaisValidasDeveLevarAoDashboard() {
        String senha = "SenhaForte123";
        User user = EntityBuilder.createRandomUser(passwordEncoder.encode(senha));
        userRepository.save(user);

        AuthPageObject authPage = new AuthPageObject(driver);
        authPage.logar(user.getEmail(), senha);

        assertThat(authPage.esperarDashboardFicarVisivel()).isTrue();
        assertThat(authPage.telaDeAutenticacaoEstaEscondida()).isTrue();
        assertThat(authPage.botaoDeLogoutEstaVisivel()).isTrue();
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Login com senha errada deve mostrar mensagem de credenciais inválidas")
    void loginComSenhaErradaDeveMostrarMensagemDeCredenciaisInvalidas() {
        User user = EntityBuilder.createRandomUser(passwordEncoder.encode("SenhaForte123"));
        userRepository.save(user);

        AuthPageObject authPage = new AuthPageObject(driver);
        authPage.logar(user.getEmail(), "SenhaErrada999");

        assertThat(authPage.esperarMensagemDeFeedback()).isEqualTo("E-mail ou senha inválidos.");
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Cadastro com dados válidos deve logar automaticamente e mostrar o dashboard")
    void cadastroComDadosValidosDeveLogarAutomaticamenteEMostrarODashboard() {
        AuthPageObject authPage = new AuthPageObject(driver);
        authPage.alternarParaAbaDeCadastro();
        authPage.cadastrar(
                EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                EntityBuilder.randomEmail(), "SenhaForte123");

        assertThat(authPage.esperarDashboardFicarVisivel()).isTrue();
        assertThat(authPage.telaDeAutenticacaoEstaEscondida()).isTrue();
        assertThat(authPage.botaoDeLogoutEstaVisivel()).isTrue();
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Cadastro com e-mail já usado deve mostrar mensagem de conta já existente")
    void cadastroComEmailJaUsadoDeveMostrarMensagemDeContaJaExistente() {
        User user = EntityBuilder.createRandomUser(passwordEncoder.encode("SenhaForte123"));
        userRepository.save(user);

        AuthPageObject authPage = new AuthPageObject(driver);
        authPage.alternarParaAbaDeCadastro();
        authPage.cadastrar(
                EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                user.getEmail(), "OutraSenha456");

        assertThat(authPage.esperarMensagemDeFeedback()).isEqualTo("Já existe uma conta com este e-mail.");
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Alternar para a aba de cadastro deve esconder o login e mostrar o formulário de cadastro")
    void alternarParaAbaDeCadastroDeveEsconderLoginEMostrarCadastro() {
        AuthPageObject authPage = new AuthPageObject(driver);

        authPage.alternarParaAbaDeCadastro();

        assertThat(authPage.formularioDeCadastroEstaVisivel()).isTrue();
        assertThat(authPage.formularioDeLoginEstaVisivel()).isFalse();
    }

    @Test
    @Tag("UiTest")
    @DisplayName("Logout deve voltar para a tela de login e esconder o botão de logout")
    void logoutDeveVoltarParaTelaDeLoginEEsconderBotaoDeLogout() {
        String senha = "SenhaForte123";
        User user = EntityBuilder.createRandomUser(passwordEncoder.encode(senha));
        userRepository.save(user);

        AuthPageObject authPage = new AuthPageObject(driver);
        authPage.logar(user.getEmail(), senha);
        assertThat(authPage.esperarDashboardFicarVisivel()).isTrue();

        authPage.sair();

        assertThat(authPage.telaDeAutenticacaoEstaEscondida()).isFalse();
        assertThat(authPage.formularioDeLoginEstaVisivel()).isTrue();
        assertThat(authPage.botaoDeLogoutEstaVisivel()).isFalse();
    }
}
