package com.esport.torneo.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Proveedor de tokens JWT para generar, validar y extraer información de tokens.
 * 
 * @author Andrés Orduz
 * @version 1.0
 * @since 2024
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    /**
     * Genera un token JWT para el usuario autenticado.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera un token JWT para un usuario específico.
     */
    public String generateTokenForUser(String username, List<String> roles) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera un refresh token.
     */
    public String generateRefreshToken(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtRefreshExpirationMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Extrae los roles del token JWT.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return (List<String>) claims.get("roles");
    }

    /**
     * Obtiene la fecha de expiración del token.
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    /**
     * Valida el token JWT.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            logger.error("Token JWT malformado");
        } catch (ExpiredJwtException ex) {
            logger.error("Token JWT expirado");
        } catch (UnsupportedJwtException ex) {
            logger.error("Token JWT no soportado");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string está vacío");
        } catch (Exception ex) {
            logger.error("Error validando token JWT", ex);
        }
        return false;
    }

    /**
     * Verifica si el token es un refresh token.
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return "refresh".equals(claims.get("type"));
        } catch (Exception ex) {
            logger.error("Error verificando tipo de token", ex);
            return false;
        }
    }

    /**
     * Obtiene el tiempo restante antes de la expiración del token (en milisegundos).
     */
    public long getTokenExpirationTime(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.getTime() - System.currentTimeMillis();
    }
} 