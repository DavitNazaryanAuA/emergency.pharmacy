package com.capstone.emergency.pharmacy.core.auth.jwt.repository;

import com.capstone.emergency.pharmacy.core.user.repository.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked ;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
