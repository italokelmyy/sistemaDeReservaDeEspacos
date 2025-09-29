package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Security.JwtSecurity;


import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final UsuarioRepository repository;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtFilter(UsuarioRepository repository, JwtUtil jwtUtil) {
        this.repository = repository;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional.ofNullable(request.getHeader("Authorization"))
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .ifPresent(token -> {
                    String usuario = jwtUtil.claims(token, Claims::getSubject);

                    if (usuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        repository.findByUsuario(usuario).ifPresent(usuario1 -> {
                            UserDetails user = User.builder()
                                    .username(usuario1.getUsuario())
                                    .password(usuario1.getSenha())
                                    .roles("USER")
                                    .build();


                            if (jwtUtil.verificarClaims(token, user)) {
                                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
                                authenticationToken.setDetails(new WebAuthenticationDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                            }


                        });
                    }

                });
        filterChain.doFilter(request, response);
    }
}
