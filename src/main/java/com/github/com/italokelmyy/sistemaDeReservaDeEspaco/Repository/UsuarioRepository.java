package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);
    Optional<Usuario> findByEmail(String email);
}
