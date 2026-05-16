package com.mfpe.adapter.in.security;

import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /**
     * Extrae los roles del claim {@code realm_access.roles} del JWT de Keycloak
     * y los transforma en {@link SimpleGrantedAuthority} con el prefijo {@code ROLE_}.
     *
     * <p>Spring Security exige el prefijo {@code ROLE_} para que
     * {@code hasRole("USER")} funcione correctamente (internamente busca {@code ROLE_USER}).</p>
     *
     * @param jwt el token JWT validado por Spring Security
     * @return colección de authorities/roles del usuario
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable Collection<GrantedAuthority> convert(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");

        return roles.stream()
                // Añadimos el prefijo ROLE_ si Keycloak no lo incluye ya
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
