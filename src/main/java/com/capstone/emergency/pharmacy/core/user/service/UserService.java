package com.capstone.emergency.pharmacy.core.user.service;

import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.user.repository.Role;
import com.capstone.emergency.pharmacy.core.user.repository.User;
import com.capstone.emergency.pharmacy.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User findById(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public User findByEmailAndPassword(String email, String password) {
        return repository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new NotFoundException("Invalid email or password"));
    }

    public User addUser(
            String firstName,
            String lastName,
            String email,
            String password
    ) {
        return repository.save(
                User.builder()
                        .id(UUID.randomUUID().toString())
                        .email(email)
                        .name(firstName)
                        .lastName(lastName)
                        .password(password)
                        .verified(false)
                        .role(Role.USER)
                        .build()
        );
    }
}
