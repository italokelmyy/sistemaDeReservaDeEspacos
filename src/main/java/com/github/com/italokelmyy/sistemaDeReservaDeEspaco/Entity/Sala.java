package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity;

import jakarta.persistence.*;

@Entity
public class Sala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String area;
    private String codigo;
    private Long capacidade;
    private String localizacao;
    @Enumerated(EnumType.STRING)
    private StatusDaSala status;


    public Sala() {

    }

    public Sala(Long id, String area, String codigo , Long capacidade, String localizacao) {
        this.id = id;
        this.area = area;
        this.codigo = codigo;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
        this.status = StatusDaSala.Disponivel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Long getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Long capacidade) {
        this.capacidade = capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public StatusDaSala getStatus() {
        return status;
    }

    public void setStatus(StatusDaSala status) {
        this.status = status;
    }
}
