package com.capstone.emergency.pharmacy.rest.controller.auth;

import com.capstone.emergency.pharmacy.core.auth.AuthService;
import com.capstone.emergency.pharmacy.core.auth.jwt.JwtService;
import com.capstone.emergency.pharmacy.core.auth.model.LoginCommand;
import com.capstone.emergency.pharmacy.core.auth.model.RegisterCommand;
import com.capstone.emergency.pharmacy.rest.controller.auth.model.JWTPair;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

//    TODO implement
    @PostMapping("/google")
    public ResponseEntity<JWTPair> googelAuth(
            @RequestBody @Valid RegisterCommand registerCommand
    ) {
        final var jwtPair = authService.register(registerCommand);
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

    @PostMapping("/refresh")
    public ResponseEntity<JWTPair> refresh(
            @RequestBody @NotBlank String refreshToken
    ) {
        final var jwtPair = authService.refresh(refreshToken);
        return ResponseEntity.ok(
                new JWTPair(jwtPair[0], jwtPair[1])
        );
    }

    @PostMapping("/refresh/revoke")
    public ResponseEntity<Void> revoke(
            @RequestBody @NotBlank String refreshToken
    ) {
        jwtService.revokeRefreshToken(refreshToken);
        return ResponseEntity.ok().build();
    }
}
