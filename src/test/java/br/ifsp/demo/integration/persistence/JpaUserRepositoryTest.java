package br.ifsp.demo.integration.persistence;

import br.ifsp.demo.infrastructure.security.user.JpaUserRepository;
import br.ifsp.demo.infrastructure.security.user.User;
import br.ifsp.demo.support.EntityBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JpaUserRepositoryTest {

    @Autowired
    private JpaUserRepository sut;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void limparBanco() {
        sut.deleteAll();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("Usuário salvo deve ser recuperado por findByEmail com os mesmos dados")
    void usuarioSalvoDeveSerRecuperadoPorFindByEmailComMesmosDados() {
        User user = EntityBuilder.createRandomUser(passwordEncoder.encode("SenhaForte123"));

        sut.save(user);

        Optional<User> found = sut.findByEmail(user.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
        assertThat(found.get().getName()).isEqualTo(user.getName());
        assertThat(found.get().getLastname()).isEqualTo(user.getLastname());
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("Senha persistida deve estar criptografada com BCrypt, nunca igual ao texto puro")
    void senhaPersistidaDeveEstarCriptografadaComBCrypt() {
        String senhaPura = "SenhaForte123";
        User user = EntityBuilder.createRandomUser(passwordEncoder.encode(senhaPura));

        sut.save(user);

        User persisted = sut.findById(user.getId()).orElseThrow();

        assertThat(persisted.getPassword()).isNotEqualTo(senhaPura);
        assertThat(passwordEncoder.matches(senhaPura, persisted.getPassword())).isTrue();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("Busca com e-mail inexistente deve retornar vazio")
    void findByEmailComEmailInexistenteDeveRetornarVazio() {
        Optional<User> found = sut.findByEmail(EntityBuilder.randomEmail());

        assertThat(found).isEmpty();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("Busca com e-mail já cadastrado porém em maiúsuclo deveria retornar o e-email cadastrado correspondente")
    void findByEmailDeveriaIgnorarCaixaDoEmailMasEhCaseSensitive() {
        User user = EntityBuilder.createRandomUser(passwordEncoder.encode("SenhaForte123"));

        sut.save(user);

        Optional<User> found = sut.findByEmail(user.getEmail().toUpperCase());

        assertThat(found).isPresent();
    }

    @Test
    @Tag("PersistenceTest")
    @Tag("IntegrationTest")
    @DisplayName("Banco deveria impedir dois usuários com o mesmo e-mail, mas aceita")
    void bancoDeveriaImpedirEmailDuplicadoMasAceitaDoisUsuariosComMesmoEmail() {
        String email = EntityBuilder.randomEmail();
        User primeiro = EntityBuilder.createUser(
                EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                email, passwordEncoder.encode("SenhaForte123"));
        User segundo = EntityBuilder.createUser(
                EntityBuilder.randomFirstName(), EntityBuilder.randomLastName(),
                email, passwordEncoder.encode("OutraSenha456"));

        sut.save(primeiro);

        assertThatThrownBy(() -> {
            sut.save(segundo);
            sut.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

}
