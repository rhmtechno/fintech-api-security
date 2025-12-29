package com.gp.poc.noncepoc.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.issuer}")
    private String issuer;

    @Value("${spring.jwt.access-token-ttl-seconds}")
    private long accessTokenTtl;

    public String generateToken(String userId, String deviceId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuer(issuer)
                .claim("deviceId", deviceId)
                .setIssuedAt(new Date())
                .setExpiration(
                        Date.from(Instant.now().plusSeconds(accessTokenTtl))
                )
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }
}