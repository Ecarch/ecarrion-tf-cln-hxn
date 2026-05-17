package com.mfpe.adapter.in.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── 1. Deshabilitar CSRF ────────────────────────────────────────────────
                // No necesario en APIs REST stateless con JWT. CSRF protege contra ataques
                // basados en cookies de sesión, que aquí no usamos.
                .csrf(csrf -> csrf.disable())

                // ── 2. Sin sesiones HTTP ────────────────────────────────────────────────
                // Cada petición es independiente. El estado de autenticación vive en el JWT,
                // no en el servidor. Esto es fundamental para escalabilidad horizontal.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── 3. Reglas de autorización por endpoint ──────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // Endpoints de actuator: acceso libre (solo para desarrollo)
                        .requestMatchers("/actuator/**").permitAll()

                        // Crear una orden: solo ADMIN puede hacerlo
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("ADMIN")

                        // Pagar o cancelar: ADMIN o USER (son acciones sobre una orden existente)
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/pay").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/cancel").hasAnyRole("ADMIN", "USER")

                        // Añadir items: ADMIN o USER
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/items").hasAnyRole("ADMIN", "USER")

                        // Consultar orden por ID: cualquier usuario autenticado (sin rol específico)
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").authenticated()

                        // Cualquier otra petición: requiere autenticación
                        .anyRequest().authenticated()
                )

                // ── 4. Configurar como OAuth2 Resource Server con JWT ───────────────────
                // Le decimos a Spring Security que este servidor valida tokens JWT
                // emitidos por Keycloak. La URL del issuer se configura en application.yml.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }

}
