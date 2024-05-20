package com.capstone.emergency.pharmacy.core.auth.jwt.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndUser_Id(String token, String userId);

    Optional<RefreshToken> findByAccessToken(String accessToken);

    List<RefreshToken> findRefreshTokenByUser_Id(String userId, Sort sort);
}
