package com.esport.torneo.config;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Convertidor personalizado de JWT a Authentication.
 * 
 * Extrae roles y authorities del token JWT y los convierte
 * en objetos de autorización de Spring Security.
 * 
 * @author Andrés Orduz Grimaldo
 * @version 1.0.0
 * @since 2024
 */
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLES_CLAIM = "roles";
    private static final String AUTHORITIES_CLAIM = "authorities";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extrae las autoridades del token JWT.
     * 
     * @param jwt token JWT
     * @return colección de autoridades
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();

        // Extraer roles del claim "roles"
        if (jwt.hasClaim(ROLES_CLAIM)) {
            Collection<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
            if (roles != null) {
                authorities.addAll(
                    roles.stream()
                        .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                );
            }
        }

        // Extraer authorities del claim "authorities"
        if (jwt.hasClaim(AUTHORITIES_CLAIM)) {
            Collection<String> auths = jwt.getClaimAsStringList(AUTHORITIES_CLAIM);
            if (auths != null) {
                authorities.addAll(
                    auths.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
                );
            }
        }

        // Si no hay roles definidos, asignar rol USER por defecto
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + "USER"));
        }

        return authorities;
    }
} 