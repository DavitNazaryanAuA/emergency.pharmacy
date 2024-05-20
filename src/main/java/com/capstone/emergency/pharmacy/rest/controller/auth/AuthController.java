package com.capstone.emergency.pharmacy.rest.controller.auth;

import com.capstone.emergency.pharmacy.core.auth.AuthService;
import com.capstone.emergency.pharmacy.core.auth.jwt.JwtService;
import com.capstone.emergency.pharmacy.core.auth.model.Oauth2ExternalLoginCommand;
import com.capstone.emergency.pharmacy.core.auth.model.LoginCommand;
import com.capstone.emergency.pharmacy.core.auth.model.RefreshCommand;
import com.capstone.emergency.pharmacy.core.auth.model.RegisterCommand;
import com.capstone.emergency.pharmacy.rest.controller.auth.model.JWTPair;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/sign-up")
    public ResponseEntity<JWTPair> signUp(
            @RequestBody @Valid RegisterCommand registerCommand
    ) {
        final var jwtPair = authService.register(registerCommand);
        return ResponseEntity.ok(
                new JWTPair(jwtPair[0], jwtPair[1])
        );
    }

    @PostMapping("/google")
    public ResponseEntity<JWTPair> googelAuth(
            @RequestBody @Valid Oauth2ExternalLoginCommand command
    ) {
        final var jwtPair = authService.googleLogin(command);
        return ResponseEntity.ok(
                new JWTPair(jwtPair[0], jwtPair[1])
        );
    }

    @PostMapping("/facebook")
    public ResponseEntity<JWTPair> facebookAuth(
            @RequestBody @Valid Oauth2ExternalLoginCommand command
    ) {
        final var jwtPair = authService.facebookLogin(command);
        return ResponseEntity.ok(
                new JWTPair(jwtPair[0], jwtPair[1])
        );
    }

    @PostMapping("/sign-in")
    public ResponseEntity<JWTPair> singIn(
            @RequestBody @Valid LoginCommand loginCommand
    ) {
        final var jwtPair = authService.login(loginCommand);
        return ResponseEntity.ok(
                new JWTPair(jwtPair[0], jwtPair[1])
        );
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> emailVerify(
            @RequestParam("token") String token
    ) {
        authService.verifyUserEmail(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-retry")
    public ResponseEntity<Void> retryEmailVerification() {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        authService.sendEmailVerifyEmail(userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JWTPair> refresh(@RequestBody @Valid RefreshCommand command) {

        try {
            final var jwtPair = authService.refresh(command.refreshToken());
            return ResponseEntity.ok(
                    new JWTPair(jwtPair[0], jwtPair[1])
            );
        } catch (Exception e) {
        }

        return null;
    }

    @PostMapping("/refresh/revoke")
    public ResponseEntity<Void> revoke(
            @RequestBody @NotBlank String refreshToken
    ) {
        jwtService.revokeRefreshToken(refreshToken);
        return ResponseEntity.ok().build();
    }
}
