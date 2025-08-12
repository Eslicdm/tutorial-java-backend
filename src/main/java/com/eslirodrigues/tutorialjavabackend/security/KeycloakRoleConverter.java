package com.eslirodrigues.tutorialjavabackend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(KeycloakRoleConverter.class);

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        log.debug("Starting role conversion for JWT with claims: {}", jwt.getClaims());

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            log.warn("JWT token does not contain 'realm_access' claim. Returning empty authorities.");
            return Collections.emptyList();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");

        if (roles == null || roles.isEmpty()) {
            log.warn("'realm_access' claim exists but contains no roles. Returning empty authorities.");
            return Collections.emptyList();
        }

        log.debug("Found roles in token: {}", roles);

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(roleName -> "ROLE_" + roleName)
                .peek(authorityName -> log.debug("Mapping role to authority: {}", authorityName))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        log.info("Successfully granted authorities: {}", authorities);
        return authorities;
    }
}