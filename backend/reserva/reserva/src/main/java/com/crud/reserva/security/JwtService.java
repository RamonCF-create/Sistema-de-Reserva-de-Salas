package com.crud.reserva.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.security.Key;

import java.util.Date;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMillis;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.key =
                Keys.hmacShaKeyFor(
                        secret.getBytes()
                );
        this.expirationMillis = expirationMinutes * 60 * 1000;
    }

    public String gerarToken(
            String email,
            String role
    ) {

        return Jwts.builder()

                .setSubject(email)

                .claim(
                        "role",
                        role
                )

                .setIssuedAt(
                        new Date()
                )

                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expirationMillis
                        )
                )

                .signWith(
                        key,
                        SignatureAlgorithm.HS256
                )

                .compact();
    }

    public String extrairEmail(
            String token
    ) {

        return extrairClaims(token)
                .getSubject();
    }

    public String extrairRole(
            String token
    ) {

        return extrairClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    public boolean tokenValido(
            String token
    ) {

        try {

            extrairClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    private Claims extrairClaims(
            String token
    ) {

        return Jwts.parserBuilder()

                .setSigningKey(key)

                .build()

                .parseClaimsJws(token)

                .getBody();
    }
}
