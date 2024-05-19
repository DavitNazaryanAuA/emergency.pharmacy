package com.capstone.emergency.pharmacy.config;

import com.capstone.emergency.pharmacy.core.auth.jwt.JwtService;
import com.capstone.emergency.pharmacy.core.user.repository.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers(HttpMethod.POST, "api/auth/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "api/item/")
                        .hasAuthority(Role.ADMIN.name())
                                .requestMatchers(HttpMethod.POST, "api/item/product")
                                .hasAuthority(Role.ADMIN.name())
                                .requestMatchers(HttpMethod.POST, "api/vm/")
                                .hasAuthority(Role.ADMIN.name())
                                .requestMatchers(HttpMethod.POST, "api/vm/{id}/items")
                                .permitAll()
                                .requestMatchers(HttpMethod.POST, "api/order/stripe-webhook")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "api/order")
                                .permitAll()
//                                .hasAuthority(Role.ADMIN.name())
                                .anyRequest().hasAuthority(Role.USER.name())
                )
                .addFilterBefore(
                        (request, response, chain) -> {
                            final var requestCasted = (HttpServletRequest) request;
                            String path = requestCasted.getRequestURI().substring(requestCasted.getContextPath().length());

                            if (
                                    !path.equals("/api/auth/sign-up") &&
                                            !path.equals("/api/auth/sign-in") &&
                                            !path.equals("/api/auth/refresh") &&
                                            !path.equals("/api/order/stripe-webhook") &&
                                            !path.equals("/api/order")

                            ) {
                                final var auth = SecurityContextHolder.getContext().getAuthentication();
                                if (auth.getPrincipal() instanceof final Jwt jwt) {
                                    final var isBlacklisted = jwtService.isBlacklisted(jwt.getTokenValue());
                                    if (isBlacklisted) {
                                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired token.");
                                    }
                                }
                            }

                            chain.doFilter(request, response);
                        },
                        AuthorizationFilter.class
                )
                .oauth2ResourceServer(oath2 -> oath2.jwt(Customizer.withDefaults()))
                .logout(logout ->
                        logout.logoutUrl("/api/auth/logout")
                                .addLogoutHandler(logoutHandler())
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {
        final var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        final var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    private LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return;
            }
            jwt = authHeader.substring(7);

            jwtService.blacklistAccessToken(jwt);
            final var refresh = jwtService.findRefreshWithAccess(jwt);
            if (refresh.isEmpty()) {
                return;
            }
            jwtService.revokeRefreshToken(refresh.get().getToken());
        };
    }
}
