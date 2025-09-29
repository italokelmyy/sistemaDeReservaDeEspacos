package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Security.JwtSecurity;


import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;
    @Value("${JWT_EXPIRATION}")
    private Long expiration;

    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }

    public Long getExpiration() {
        return expiration;
    }
}
