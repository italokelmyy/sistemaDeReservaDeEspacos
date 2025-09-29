package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Controller;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Usuario;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastroDoUsuario(@RequestBody @Valid Usuario usuario) {
        return service.cadastro(usuario);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody Usuario usuario) {
        return service.login(usuario);
    }

}
