package com.capstone.emergency.pharmacy.core.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    User findByGoogleId(String googleId);

    User findByFacebookId(String googleId);
}
