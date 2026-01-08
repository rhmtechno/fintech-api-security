package com.gp.poc.noncepoc.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtFilter;
        private final NonceSecurityFilter nonceFilter;
        private final IdempotencyFilter idempotencyFilter;
        private final RateLimitingFilter rateLimitingFilter;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http)
                        throws Exception {

                return http
                                .csrf(AbstractHttpConfigurer::disable)

                                // RATE LIMITING
                                .addFilterBefore(
                                                rateLimitingFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                // WHO
                                .addFilterBefore(
                                                jwtFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                // REPLAY + INTEGRITY
                                .addFilterAfter(
                                                nonceFilter,
                                                JwtAuthenticationFilter.class)
                                .addFilterAfter(
                                                idempotencyFilter,
                                                NonceSecurityFilter.class)

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/auth/**").permitAll()
                                                .requestMatchers("/internal/**").permitAll()
                                                .requestMatchers("/api/**").authenticated()
                                                .anyRequest().permitAll())
                                .build();
        }
}