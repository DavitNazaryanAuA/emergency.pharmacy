package com.capstone.emergency.pharmacy.core.user.repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usr")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {

    @Id
    private String id;

    @Column(name = "stripe_id")
    private String stripeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "verified", nullable = false)
    private Boolean verified;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
}
