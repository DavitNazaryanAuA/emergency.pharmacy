package com.capstone.emergency.pharmacy.core.auth.jwt;

import com.capstone.emergency.pharmacy.core.auth.jwt.repository.RefreshToken;
import com.capstone.emergency.pharmacy.core.auth.jwt.repository.RefreshTokenRepository;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.error.UnauthorizedException;
import com.capstone.emergency.pharmacy.core.user.repository.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public String[] accessRefreshPair(User user) {
//        TODO change exp to 10 minutes after development
        final var accessExp = Instant.now().plus(20, ChronoUnit.DAYS);
        final var refreshExp = Instant.now().plus(7, ChronoUnit.DAYS);

        final var refreshToken = generateToken(user, refreshExp);
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .isRevoked(false)
                .build()
        );
        return new String[]{
                generateToken(user, accessExp),
                refreshToken
        };
    }

    public Jwt validateRefresh(String refreshToken) {
        final var token = jwtDecoder.decode(refreshToken);
        final var isExpired = token.getExpiresAt().isBefore(Instant.now());

        if (isExpired) {
            throw new UnauthorizedException("Refresh token expired");
        }
        refreshTokenRepository
                .findByTokenAndUser_Id(refreshToken, token.getSubject())
                .ifPresent(refresh1 -> {
                    if (refresh1.getIsRevoked()) {
                        throw new UnauthorizedException("Refresh token has been revoked");
                    }
                });

        return token;
    }

    public void revokeRefreshToken(String refreshToken) {
        final var decoded = jwtDecoder.decode(refreshToken);

        final var token = refreshTokenRepository.findByTokenAndUser_Id(
                refreshToken, decoded.getSubject()
        ).orElseThrow(() -> new NotFoundException("Refresh token not found for user with id: " + decoded.getSubject()));

        token.setIsRevoked(true);
        refreshTokenRepository.save(token);
    }

    private String generateToken(User user, Instant exp) {
        final var role = user
                .getRole()
                .name();

        final var token = JwtClaimsSet.builder()
                .claim("role", role)
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(exp)
                .subject(user.getId())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(token)).getTokenValue();
    }
}
