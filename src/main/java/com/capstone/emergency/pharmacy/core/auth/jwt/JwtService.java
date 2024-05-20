package com.capstone.emergency.pharmacy.core.auth.jwt;

import com.capstone.emergency.pharmacy.core.auth.jwt.repository.RefreshToken;
import com.capstone.emergency.pharmacy.core.auth.jwt.repository.RefreshTokenRepository;
import com.capstone.emergency.pharmacy.core.auth.jwt.repository.TokenRedisRepository;
import com.capstone.emergency.pharmacy.core.error.ForbiddenException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.error.UnauthorizedException;
import com.capstone.emergency.pharmacy.core.user.repository.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
    private Integer refreshTokenExpDays;

    public String[] accessRefreshPair(User user) {
        final var accessExp = Instant.now().plus(accessTokenExpMinutes, ChronoUnit.MINUTES);
        final var refreshExp = Instant.now().plus(refreshTokenExpDays, ChronoUnit.DAYS);

        final var refreshToken = generateToken(user, refreshExp);
        final var accessToken = generateToken(user, accessExp);
        refreshTokenRepository.save(RefreshToken.builder()
                .accessToken(accessToken)
                .token(refreshToken)
                .user(user)
                .date(new Date())
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

    public Jwt validateAccessToken(String accessToken) {
        final var token = jwtDecoder.decode(accessToken);
        if (token.getExpiresAt().isBefore(Instant.now()) || isBlacklisted(accessToken)) {
            throw new ForbiddenException("Token expired");
        }
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

    public String generateToken(User user, Instant exp) {
        final var role = user
                .getRole()
                .name();

        final var token = JwtClaimsSet.builder()
                .claim("role", role)
                .claim("emailVerified", user.getVerified())
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(exp)
                .subject(user.getId())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(token)).getTokenValue();
    }

    public void invalidateLastTokenPair(String userId) {
        final var refresh = refreshTokenRepository
                .findRefreshTokenByUser_Id(userId, Sort.by(Sort.Direction.DESC, "date"))
                .get(0);

        if (refresh != null) {
            final var access = refresh.getAccessToken();
            tokenRedisRepository.blacklistAccessToken(access);
            refresh.setIsRevoked(true);
            refreshTokenRepository.save(refresh);
        }
    }
}
