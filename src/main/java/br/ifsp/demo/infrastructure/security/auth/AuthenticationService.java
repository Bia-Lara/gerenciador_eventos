package br.ifsp.demo.infrastructure.security.auth;

import br.ifsp.demo.infrastructure.security.config.JwtService;
import br.ifsp.demo.infrastructure.security.user.FakeUserStore;
import br.ifsp.demo.infrastructure.security.user.JpaUserRepository;
import br.ifsp.demo.infrastructure.security.user.Role;
import br.ifsp.demo.infrastructure.security.user.User;
import br.ifsp.demo.exception.EntityAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final FakeUserStore userStore;
    private final JpaUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(FakeUserStore userStore, JpaUserRepository userRepository,
                                 JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userStore = userStore;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterUserResponse register(RegisterUserRequest request) {
        if (request == null || isBlank(request.name()) || isBlank(request.lastname())
                || isBlank(request.email()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Name, lastname, email and password are required");
        }
        if (request.password().length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters");
        }

        String email = normalizeEmail(request.email());
        if (userRepository.findByEmail(email).isPresent() || userStore.findByEmail(email).isPresent()) {
            throw new EntityAlreadyExistsException("User email is already registered");
        }

        User user = new User(UUID.randomUUID(), request.name().trim(), request.lastname().trim(),
                email, passwordEncoder.encode(request.password()), Role.USER);
        return new RegisterUserResponse(userRepository.save(user).getId());
    }

    public AuthResponse authenticate(AuthRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new IllegalArgumentException("Email and password are required");
        }

        String email = normalizeEmail(request.username());
        Optional<User> registeredUser = userRepository.findByEmail(email);
        final User user = registeredUser.or(() -> userStore.findByEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        boolean validPassword = registeredUser
                .map(persistedUser -> passwordEncoder.matches(request.password(), persistedUser.getPassword()))
                .orElseGet(() -> FakeUserStore.FAKE_PASSWORD.equals(request.password()));
        if (!validPassword) {
            throw new IllegalArgumentException("Invalid password");
        }

        final String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
