package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Security.JwtSecurity;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final JwtConfig jwtConfig;

    @Autowired
    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String generadorDeKey(UserDetails userDetails) {
        return Jwts.builder()
                .signWith(jwtConfig.secretKey())
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .issuedAt(new Date())
                .compact();
    }


    public Claims claimsAll(String key) {
        return Jwts.parser()
                .verifyWith(jwtConfig.secretKey())
                .build()
                .parseSignedClaims(key)
                .getPayload();
    }


    public <T> T claims(String key, Function<Claims, T> resolver) {
        return resolver.apply(claimsAll(key));
    }


    public boolean verificarClaims(String key, UserDetails userDetails) {
        String usuario = claims(key, Claims::getSubject);
        return usuario.equals(userDetails.getUsername()) && !verificarValidade(key);
    }

    private boolean verificarValidade(String key) {
        return claims(key, Claims::getExpiration).before(new Date());
    }


}
