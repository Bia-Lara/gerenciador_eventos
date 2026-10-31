package br.ifsp.demo.infrastructure.repository;

import br.ifsp.demo.application.user.UserRepository;
import br.ifsp.demo.infrastructure.security.user.FakeUserStore;
import br.ifsp.demo.infrastructure.security.user.JpaUserRepository;
import br.ifsp.demo.infrastructure.security.user.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class FakeUserRepository implements UserRepository {

    private final FakeUserStore userStore;
    private final JpaUserRepository userRepository;

    public FakeUserRepository(FakeUserStore userStore, JpaUserRepository userRepository) {
        this.userStore = userStore;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).or(() -> userStore.findById(id));
    }
}
