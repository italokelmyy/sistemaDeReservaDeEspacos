package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Usuario;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.UsuarioRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Security.JwtSecurity.JwtUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;



@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager manager;
    private final JwtUtil jwtUtil;

    public UsuarioService(UsuarioRepository repository, @Lazy PasswordEncoder passwordEncoder, @Lazy AuthenticationManager manager, JwtUtil jwtUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.manager = manager;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<?> cadastro(Usuario usuario) {

        if (repository.findByUsuario(usuario.getUsuario()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário já cadastrado");
        } else if (repository.findByEmail(usuario.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        repository.save(usuario);
        return ResponseEntity.ok("Usuário cadastrado com sucesso");
    }


    public ResponseEntity<?> login(Usuario usuario) {
        manager.authenticate(new UsernamePasswordAuthenticationToken(usuario.getUsuario(), usuario.getSenha()));

        Optional<Usuario> loginUsuario = repository.findByUsuario(usuario.getUsuario());

        if (loginUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não cadastrado");
        }


        Usuario usuario1 = loginUsuario.get();

        UserDetails user = User.builder()
                .username(usuario1.getUsuario())
                .password(usuario1.getSenha())
                .roles("USER")
                .build();



        return ResponseEntity.ok(jwtUtil.generadorDeKey(user));
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = repository.findByUsuario(username).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));


        return User.builder()
                .username(usuario.getUsuario())
                .password(usuario.getSenha())
                .roles("USER")
                .build();
    }


}
