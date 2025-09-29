package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.UsuarioTest;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Usuario;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.UsuarioRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Security.JwtSecurity.JwtUtil;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.UsuarioService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;


public class UsuarioTest {

    @Mock
    private UsuarioRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager manager;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UsuarioService service;

    @BeforeEach
    void testDown() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void sucesso_Ao_cadastrar_O_usuario() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsuario("teste-t");
        usuario.setSenha(passwordEncoder.encode("senhasegura123"));
        usuario.setEmail("teste12@hotmail.com");

        Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

        ResponseEntity<?> usuarioCadastrado = service.cadastro(usuario);

        Assertions.assertEquals(HttpStatus.OK, usuarioCadastrado.getStatusCode());
        Assertions.assertEquals("Usuário cadastrado com sucesso", usuarioCadastrado.getBody());
        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Usuario.class));
    }


    @Test
    void erro_AoCadastrarUsuarioComOMesmoNome() {

        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setUsuario("teste-t");
        usuario1.setSenha(passwordEncoder.encode("senhasegura123"));
        usuario1.setEmail("teste12@hotmail.com");

        Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario1);
        Mockito.when(repository.findByUsuario(usuario1.getUsuario())).thenReturn(Optional.of(usuario1));

        Usuario usuarioJaCadastrado = new Usuario();
        usuarioJaCadastrado.setId(1L);
        usuarioJaCadastrado.setUsuario("teste-t");
        usuarioJaCadastrado.setSenha(passwordEncoder.encode("senhasegura123"));
        usuarioJaCadastrado.setEmail("teste12@hotmail.com");

        ResponseEntity<?> erroUsuario = service.cadastro(usuarioJaCadastrado);

        Assertions.assertEquals(HttpStatus.CONFLICT, erroUsuario.getStatusCode());
        Assertions.assertEquals("Usuário já cadastrado", erroUsuario.getBody());

        Mockito.verify(repository, Mockito.times(1)).findByUsuario(usuarioJaCadastrado.getUsuario());
    }


    @Test
    void erro_AoCadastrarEmailQueJaFoiCadastrado() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setUsuario("teste-t");
        usuario1.setSenha(passwordEncoder.encode("senhasegura123"));
        usuario1.setEmail("teste12@hotmail.com");

        Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario1);
        Mockito.when(repository.findByEmail(usuario1.getEmail())).thenReturn(Optional.of(usuario1));

        Usuario emailJaCadastrado = new Usuario();
        emailJaCadastrado.setId(1L);
        emailJaCadastrado.setUsuario("teste-t2");
        emailJaCadastrado.setSenha(passwordEncoder.encode("senhasegura123"));
        emailJaCadastrado.setEmail("teste12@hotmail.com");

        ResponseEntity<?> erroEmail = service.cadastro(emailJaCadastrado);

        Assertions.assertEquals(HttpStatus.CONFLICT, erroEmail.getStatusCode());
        Assertions.assertEquals("E-mail já cadastrado", erroEmail.getBody());

        Mockito.verify(repository, Mockito.times(1)).findByEmail(emailJaCadastrado.getEmail());
    }

    @Test
    void Sucesso_AoFazerLoginERetornarOToken() {

        String token = "token";
        String senhaCriptografada = "senhaCriptografada";

        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setUsuario("teste-t");
        usuario1.setSenha(senhaCriptografada);
        usuario1.setEmail("teste12@hotmail.com");

        Usuario loginUsuario = new Usuario();
        loginUsuario.setUsuario("teste-t");
        loginUsuario.setSenha(senhaCriptografada);

        UserDetails userDetails = User.builder()
                .username(loginUsuario.getUsuario())
                .password(senhaCriptografada)
                .roles("USER")
                .build();

        Mockito.when(repository.findByUsuario(usuario1.getUsuario())).thenReturn(Optional.of(usuario1));

        Mockito.when(jwtUtil.generadorDeKey(userDetails)).thenReturn(token);

        ResponseEntity<?> loginToken = service.login(loginUsuario);

        Assertions.assertEquals(token, loginToken.getBody());
        Assertions.assertEquals(HttpStatus.OK, loginToken.getStatusCode());

        Mockito.verify(repository, Mockito.times(1)).findByUsuario(loginUsuario.getUsuario());
        Mockito.verify(manager, Mockito.times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(loginUsuario.getUsuario(), loginUsuario.getSenha())
        );
    }


    @Test
    void erro_UsuarioNaoEncontrado() {
        String username = "usuárioInexistente";

        Usuario usuario1 = new Usuario();
        usuario1.setUsuario(username);

        Mockito.when(repository.findByUsuario(usuario1.getUsuario())).thenReturn(Optional.empty());

        ResponseEntity<?> erroUsername = service.login(usuario1);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, erroUsername.getStatusCode());
        Assertions.assertEquals("Usuário não cadastrado", erroUsername.getBody());
        Mockito.verify(repository, Mockito.times(1)).findByUsuario(usuario1.getUsuario());
    }


    @Test
    void loadUserByUsername() {
        String username = "teste1";
        String senhaCriptografada = "senhaCriptografada";

        Usuario usuario1 = new Usuario();
        usuario1.setUsuario(username);
        usuario1.setSenha(senhaCriptografada);
        usuario1.setEmail("test@test.com");

        Mockito.when(repository.findByUsuario(username)).thenReturn(Optional.of(usuario1));


        UserDetails user = service.loadUserByUsername(username);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(username, user.getUsername());


        Mockito.verify(repository, Mockito.times(1)).findByUsuario(username);
    }

    @Test
    void erro_DeveLancarExcecaoAoBuscarUsuarioInexistente() {

        String username = "usuárioInexistente";

        Mockito.when(repository.findByUsuario(username)).thenReturn(Optional.empty());


        Assertions.assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(username));
        Mockito.verify(repository, Mockito.times(1)).findByUsuario(username);
    }

}
