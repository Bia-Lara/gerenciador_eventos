package br.ifsp.demo.infrastructure.security.auth;

import br.ifsp.demo.infrastructure.security.config.JwtService;
import br.ifsp.demo.infrastructure.security.user.FakeUserStore;
import br.ifsp.demo.infrastructure.security.user.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final FakeUserStore userStore;
    private final JwtService jwtService;

    public AuthenticationService(FakeUserStore userStore, JwtService jwtService) {
        this.userStore = userStore;
        this.jwtService = jwtService;
    }

    public RegisterUserResponse register(RegisterUserRequest request) {
        User user = userStore.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Users are fake; use a predefined fake email"));
        return new RegisterUserResponse(user.getId());
    }

    public AuthResponse authenticate(AuthRequest request) {
        final User user = userStore.findByEmail(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        if (!FakeUserStore.FAKE_PASSWORD.equals(request.password())) {
            throw new IllegalArgumentException("Invalid password");
        }

        final String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}
