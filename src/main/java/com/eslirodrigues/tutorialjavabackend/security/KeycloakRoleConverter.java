package com.eslirodrigues.tutorialjavabackend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null || resourceAccess.isEmpty()) return Collections.emptyList();

        String clientId = jwt.getClaimAsString("azp");
        if (clientId == null) return Collections.emptyList();
        if (!(resourceAccess.get(clientId) instanceof Map)) return Collections.emptyList();

        Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
        if (!(clientAccess.get("roles") instanceof Collection)) return Collections.emptyList();

        Collection<String> roles = (Collection<String>) clientAccess.get("roles");

        return roles.stream()
                .map(roleName -> "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}