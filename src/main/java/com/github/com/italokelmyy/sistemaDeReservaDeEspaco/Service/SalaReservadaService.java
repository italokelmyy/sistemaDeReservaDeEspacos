package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Configuration.RabbitMqConfig;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.SalaResevada;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.StatusDaSala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalaResevadaRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalasRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;


@Service
public class SalaReservadaService {
    private final SalaResevadaRepository resevadaRepository;
    private final SalasRepository salasRepository;
    private final RabbitTemplate rabbitTemplate;

    public SalaReservadaService(SalaResevadaRepository resevadaRepository, SalasRepository salasRepository, RabbitTemplate rabbitTemplate) {
        this.resevadaRepository = resevadaRepository;
        this.salasRepository = salasRepository;
        this.rabbitTemplate = rabbitTemplate;
    }


    public ResponseEntity<?> reservaSala(Long id, String nomeDoResponsavel, String horarioAgendado) {
        Optional<Sala> findById = salasRepository.findById(id);


        if (findById.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sala não encontrada");
        }

        Sala reservado = findById.get();

        LocalTime novoHorario;
        try {
            novoHorario = LocalTime.parse(horarioAgendado);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato de horário inválido. Use HH:mm (ex: 14:00)");
        }


        List<SalaResevada> reservasExistentes = resevadaRepository.findBySalaId(reservado.getId());

        for (SalaResevada reserva : reservasExistentes) {
            LocalTime horarioExistente = LocalTime.parse(reserva.getHorarioAgendado());
            long minutos = Duration.between(horarioExistente, novoHorario).abs().toMinutes();

            if (minutos < 30) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Já existe uma reserva próxima a esse horário. É necessário um intervalo de pelo menos 30 minutos.");
            }
        }


        SalaResevada novaReserva = new SalaResevada();
        novaReserva.setSalaId(reservado.getId());
        novaReserva.setResponsavel_pela_sala(nomeDoResponsavel);
        novaReserva.setCodigo_da_sala(reservado.getCodigo());
        novaReserva.setHorarioAgendado(horarioAgendado);
        novaReserva.setStatus(StatusDaSala.Ocupada);

        resevadaRepository.save(novaReserva);
        if (novaReserva.getStatus() == StatusDaSala.Ocupada) {
            System.out.println("Publicando objeto Tarefa como JSON para a fila: " + novaReserva.getSalaId());
            rabbitTemplate.convertAndSend(RabbitMqConfig.SALARESERVADA, novaReserva);
        }

        return ResponseEntity.ok("Sala adicionada com sucesso");
    }


    public ResponseEntity<?> mostarSalasReservadas() {
        List<SalaResevada> salaResevada = resevadaRepository.findAll();

        if (salaResevada.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma sala reservada");
        }

        return ResponseEntity.ok(salaResevada);
    }


    public ResponseEntity<?> removerSalaReservada(Long id) {
        resevadaRepository.deleteById(id);
        return ResponseEntity.ok("Sala removida com sucesso");
    }

}
