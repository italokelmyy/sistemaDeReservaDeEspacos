package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.UsuarioTest;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Usuario;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.UsuarioRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Security.JwtSecurity.JwtUtil;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class UsuarioTestIntegration {

    @Autowired
    private UsuarioRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UsuarioService service;

    @AfterEach
    void testDown() {
        repository.deleteAll();
    }

    @Test
    void sucessoAoCadastrarUsuario() {

        Usuario usuario = new Usuario();
        usuario.setUsuario("test1");
        usuario.setSenha(passwordEncoder.encode("senhaSegura123"));
        usuario.setEmail("test12@hotmail.com");


        ResponseEntity<?> cadastroSucesso = service.cadastro(usuario);

        Assertions.assertEquals(HttpStatus.OK, cadastroSucesso.getStatusCode());
        Assertions.assertEquals("Usuário cadastrado com sucesso", cadastroSucesso.getBody());
    }


    @Test
    void deveRetornarErroUsuarioJaCadastrado() {

        Usuario usuario = new Usuario();
        usuario.setUsuario("test1");
        usuario.setSenha(passwordEncoder.encode("senhaSegura123"));
        usuario.setEmail("test12@hotmail.com");

        repository.save(usuario);

        Usuario usuarioJaCadastrado = new Usuario();
        usuarioJaCadastrado.setUsuario(usuario.getUsuario());


        ResponseEntity<?> erroUser = service.cadastro(usuarioJaCadastrado);

        Assertions.assertEquals(HttpStatus.CONFLICT, erroUser.getStatusCode());
        Assertions.assertEquals("Usuário já cadastrado", erroUser.getBody());
    }

    @Test
    void deveRetornarErroEmailCadastrado() {
        Usuario usuario = new Usuario();
        usuario.setUsuario("test1");
        usuario.setSenha(passwordEncoder.encode("senhaSegura123"));
        usuario.setEmail("test12@hotmail.com");

        repository.save(usuario);

        Usuario emailJaCadastrado = new Usuario();
        emailJaCadastrado.setEmail(usuario.getEmail());

        ResponseEntity<?> erroEmail = service.cadastro(emailJaCadastrado);

        Assertions.assertEquals(HttpStatus.CONFLICT, erroEmail.getStatusCode());
        Assertions.assertEquals("E-mail já cadastrado", erroEmail.getBody());
    }

    @Test
    void sucessoAoFazerLogin() {

        String senha = "senhaSegura123";


        Usuario usuario = new Usuario();
        usuario.setUsuario("test1");
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setEmail("test12@hotmail.com");

        repository.save(usuario);

        manager.authenticate(new UsernamePasswordAuthenticationToken(usuario.getUsuario(), senha));

        Usuario login = new Usuario();
        login.setUsuario("test1");
        login.setSenha(senha);


        UserDetails user = User.builder()
                .username(login.getUsuario())
                .password(senha)
                .roles("USER")
                .build();


        String generatedToken = jwtUtil.generadorDeKey(user);
        ResponseEntity<?> sucessLogin = service.login(login);

        Assertions.assertEquals(HttpStatus.OK, sucessLogin.getStatusCode());
        Assertions.assertNotNull(generatedToken);
        Assertions.assertFalse(generatedToken.isEmpty());
    }


    @Test
    void deveRetornarErroDeUsuarioNaoEncontrado() {
        String senha = "senhaSegura123";


        Usuario usuario = new Usuario();
        usuario.setUsuario("test1");
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setEmail("test12@hotmail.com");

        repository.save(usuario);


        Usuario login = new Usuario();
        login.setUsuario("test2");
        login.setSenha(senha);


        Optional<Usuario> loginUsuario = repository.findByUsuario(login.getUsuario());

        Assertions.assertTrue(loginUsuario.isEmpty());
    }

    @Test
    void loadUserByUsername() {

        String senha = "senhaSegura123";


        Usuario usuario = new Usuario();
        usuario.setUsuario("test1");
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setEmail("test12@hotmail.com");

        repository.save(usuario);


        Usuario login = new Usuario();
        login.setUsuario("test1");
        login.setSenha(senha);

        Optional<Usuario> findByUsuario = repository.findByUsuario(login.getUsuario());

        UserDetails details = service.loadUserByUsername(login.getUsuario());

        Assertions.assertTrue(findByUsuario.isPresent());
        Assertions.assertNotNull(details);
    }

    @Test
    void deveRetornarErroLoadUserByUsername(){
        String username = "naoExiste";

        Optional<Usuario> findByUsuario = repository.findByUsuario(username);


        Assertions.assertTrue(findByUsuario.isEmpty());
        Assertions.assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(username));
    }





}

