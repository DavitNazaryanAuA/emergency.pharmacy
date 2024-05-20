package com.capstone.emergency.pharmacy.core.auth;

import com.capstone.emergency.pharmacy.core.auth.jwt.JwtService;
import com.capstone.emergency.pharmacy.core.auth.model.LoginCommand;
import com.capstone.emergency.pharmacy.core.auth.model.RegisterCommand;
import com.capstone.emergency.pharmacy.core.email.model.EmailDto;
import com.capstone.emergency.pharmacy.core.email.service.EmailService;
import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.user.repository.User;
import com.capstone.emergency.pharmacy.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${jwt.email.token.exp.minutes}")
    private Integer emailTokenExpMinutes;

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
        final var user = userService.findByEmail(loginCommand.email()).orElseThrow(() -> new NotFoundException("Invalid email or password"));
        if (!passwordEncoder.matches(loginCommand.password(), user.getPassword())) {
            throw new NotFoundException("Invalid email or password");
        }
        sendEmailVerifyEmail(user);
        return jwtService.accessRefreshPair(user);
    }

    public String[] refresh(String refresh) {
        final var validRefreshToken = jwtService.validateRefresh(refresh);
        final var user = userService.findById(validRefreshToken.getSubject());
        jwtService.revokeRefreshToken(refresh);

        return jwtService.accessRefreshPair(user);
    }

    public void verifyUserEmail(String token) {
        final var jwt = jwtService.validateAccessToken(token);
        userService.setVerified(jwt.getSubject());
        jwtService.invalidateLastTokenPair(jwt.getSubject());
    }

    public void sendEmailVerifyEmail(String userId) {
        final var user = userService.findById(userId);
        sendEmailVerifyEmail(user);
    }

    private void sendEmailVerifyEmail(User user) {
        final var token = jwtService.generateToken(user, Instant.now().plus(emailTokenExpMinutes, ChronoUnit.MINUTES));
        final var verificationLink = "https://a0ef-5-77-254-89.ngrok-free.app/api/auth/verify?token=" + token;

        final var email = EmailDto.builder()
                .emailTo(user.getEmail())
                .subject("Disp Email Verification")
                .text("Please use the following link to verify your email: " + verificationLink)
                .build();
        emailService.sendEmail(email);
    }
}
