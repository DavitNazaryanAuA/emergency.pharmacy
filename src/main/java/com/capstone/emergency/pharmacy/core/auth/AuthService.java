package com.capstone.emergency.pharmacy.core.auth;

import com.capstone.emergency.pharmacy.core.auth.jwt.JwtService;
import com.capstone.emergency.pharmacy.core.auth.model.LoginCommand;
import com.capstone.emergency.pharmacy.core.auth.model.RegisterCommand;
import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String[] register(RegisterCommand registerCommand) {
        final var existingUser = userService.findByEmail(registerCommand.email());
        if (existingUser.isPresent()) {
            throw new BadRequestException("Email taken");
        }
        final var user = userService.addUser(
                registerCommand.firstName(),
                registerCommand.lastName(),
                registerCommand.email(),
                passwordEncoder.encode(registerCommand.password())
        );

        return jwtService.accessRefreshPair(user);
    }

    public String[] login(LoginCommand loginCommand) {
        final var user = userService.findByEmailAndPassword(loginCommand.email(), loginCommand.password());
        return jwtService.accessRefreshPair(user);
    }

    public String[] refresh(String refresh) {
        final var validRefreshToken = jwtService.validateRefresh(refresh);
        final var user = userService.findById(validRefreshToken.getSubject());
        jwtService.revokeRefreshToken(refresh);

        return jwtService.accessRefreshPair(user);
    }
}
