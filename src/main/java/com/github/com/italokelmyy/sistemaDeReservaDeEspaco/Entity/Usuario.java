package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "Usuário é obrigatório")
    private String usuario;
    @NotNull(message = "Senha é obrigatório")
    @Size(min = 8, message = "Senha com no mínimo 8 caracteres")
    private String senha;
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_+-]+@[A-Za-z]{5,15}\\.[A-Za-z]{2,}$", message = "Formato Permitido: exemploemail@exemplo.com")
    private String email;

    public Usuario() {

    }

    public Usuario(Long id, String usuario, String senha, String email) {
        this.id = id;
        this.usuario = usuario;
        this.senha = senha;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



}
