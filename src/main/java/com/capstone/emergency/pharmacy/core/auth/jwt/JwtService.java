package com.capstone.emergency.pharmacy.core.auth.jwt;

import com.capstone.emergency.pharmacy.core.auth.jwt.repository.RefreshToken;
import com.capstone.emergency.pharmacy.core.auth.jwt.repository.RefreshTokenRepository;
import com.capstone.emergency.pharmacy.core.auth.jwt.repository.TokenRedisRepository;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.error.UnauthorizedException;
import com.capstone.emergency.pharmacy.core.user.repository.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenRedisRepository tokenRedisRepository;

    @Value("${jwt.access.exp.minutes}")
    private Integer accessTokenExpMinutes;

    @Value("${jwt.refresh.exp.days}")
    private Integer refreshTokenExpMinutes;

    public String[] accessRefreshPair(User user) {
//        TODO change exp to 10 minutes after development
        final var accessExp = Instant.now().plus(accessTokenExpMinutes, ChronoUnit.MINUTES);
        final var refreshExp = Instant.now().plus(refreshTokenExpMinutes, ChronoUnit.DAYS);

        final var refreshToken = generateToken(user, refreshExp);
        final var accessToken = generateToken(user, accessExp);
        refreshTokenRepository.save(RefreshToken.builder()
                .accessToken(accessToken)
                .token(refreshToken)
                .user(user)
                .isRevoked(false)
                .build()
        );
        return new String[]{
                accessToken,
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
        tokenRedisRepository.blacklistAccessToken(token.getAccessToken());
        refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findRefreshWithAccess(String accessToken) {
        return refreshTokenRepository.findByAccessToken(accessToken);
    }

    public void blacklistAccessToken(String token) {
        tokenRedisRepository.blacklistAccessToken(token);
    }

    public Boolean isBlacklisted(String token) {
        return tokenRedisRepository.isBlackListed(token);
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
