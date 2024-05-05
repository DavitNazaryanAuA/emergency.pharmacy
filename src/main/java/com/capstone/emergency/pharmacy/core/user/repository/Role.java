package com.capstone.emergency.pharmacy.core.user.repository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static com.capstone.emergency.pharmacy.core.user.repository.Permissions.*;


@RequiredArgsConstructor
public enum Role {
    USER(
            Set.of(
                    GET_MACHINES,
                    MAKE_PURCHASE
            )
    ),
    ADMIN(
            Set.of(
                    ADD_ITEM
            )
    );

    @Getter
    private final Set<String> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        return getPermissions()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
