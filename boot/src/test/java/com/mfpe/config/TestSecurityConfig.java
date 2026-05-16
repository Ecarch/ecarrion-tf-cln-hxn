package com.mfpe.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // En tests nunca se valida un JWT real, así que este decoder
        // nunca es invocado. Su única función es evitar que Spring Boot
        // intente conectarse a Keycloak durante el arranque del contexto.
        return token -> {
            throw new UnsupportedOperationException(
                    "JwtDecoder mock: los tests no deben enviar tokens JWT reales. " +
                            "Usa TestSecurityConfig que hace .permitAll() para todos los endpoints.");
        };
    }

}
