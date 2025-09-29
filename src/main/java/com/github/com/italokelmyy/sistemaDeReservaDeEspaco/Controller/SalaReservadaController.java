package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Controller;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.SalaResevada;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.SalaReservadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reserva")
public class SalaReservadaController {
    private final SalaReservadaService service;

    @Autowired
    public SalaReservadaController(SalaReservadaService service) {
        this.service = service;
    }

    @PostMapping("/adicionar")
    public ResponseEntity<?> adicionarSalaReservada(@RequestBody SalaResevada reservadaDTO) {
        String nome = reservadaDTO.getResponsavel_pela_sala();
        String horario = reservadaDTO.getHorarioAgendado();
        return service.reservaSala(reservadaDTO.getId(), nome, horario);
    }

    @GetMapping("/lista")
    public ResponseEntity<?> mostrarSalas() {
        return service.mostarSalasReservadas();
    }

    @GetMapping("/deleteById/{id}")
    public ResponseEntity<?>deletarPeloId(@PathVariable Long id) {
        return service.removerSalaReservada(id);
    }


}