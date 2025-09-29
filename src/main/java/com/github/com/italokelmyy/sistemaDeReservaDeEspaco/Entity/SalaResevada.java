package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "salareservada")
public class SalaResevada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long salaId;
    private String responsavel_pela_sala;
    private String codigo_da_sala;
    private String horarioAgendado;
    private StatusDaSala status;


    public SalaResevada() {

    }

    public SalaResevada(Long salaId, String responsavel_pela_sala, String codigo_da_sala, String horario_agendado) {
        this.salaId = salaId;
        this.responsavel_pela_sala = responsavel_pela_sala;
        this.codigo_da_sala = codigo_da_sala;
        this.horarioAgendado = horario_agendado;
        this.status = StatusDaSala.Ocupada;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }

    public String getResponsavel_pela_sala() {
        return responsavel_pela_sala;
    }

    public void setResponsavel_pela_sala(String responsavel_pela_sala) {
        this.responsavel_pela_sala = responsavel_pela_sala;
    }

    public String getCodigo_da_sala() {
        return codigo_da_sala;
    }

    public void setCodigo_da_sala(String codigo_da_sala) {
        this.codigo_da_sala = codigo_da_sala;
    }

    public String getHorarioAgendado() {
        return horarioAgendado;
    }

    public void setHorarioAgendado(String horarioAgendado) {
        this.horarioAgendado = horarioAgendado;
    }

    public StatusDaSala getStatus() {
        return status;
    }

    public void setStatus(StatusDaSala status) {
        this.status = status;
    }
}
