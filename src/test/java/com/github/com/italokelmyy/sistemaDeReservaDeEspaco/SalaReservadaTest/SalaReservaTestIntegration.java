package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.SalaReservadaTest;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Configuration.RabbitMqConfig;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.SalaResevada;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.StatusDaSala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalaResevadaRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalasRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.SalaReservadaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class SalaReservaTestIntegration {
    @Autowired
    private SalaResevadaRepository resevadaRepository;
    @Autowired
    private SalasRepository salasRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SalaReservadaService service;


    @Test
    void sucesso_ao_reservar_a_sala() {

        StatusDaSala status = StatusDaSala.Disponivel;

        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        salasRepository.save(sala);

        Optional<Sala> findById = salasRepository.findById(sala.getId());

        Assertions.assertTrue(findById.isPresent());

        ResponseEntity<?> response = service.reservaSala(sala.getId(), "test1", "15:00");

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Sala adicionada com sucesso", response.getBody());
    }


    @Test
    void erro_SalaNaoEncontrada() {

        Long id = 2L;

        Optional<Sala> findById = salasRepository.findById(id);

        Assertions.assertTrue(findById.isEmpty());
    }


    @Test
    void erro_formatoErradoDoHorario() {


        String responsavel = "Usuario Teste";
        String horarioInvalido = "14-30";

        Sala sala = new Sala();
        sala.setCodigo("sala-01");

        salasRepository.save(sala);


        ResponseEntity<?> response = service.reservaSala(sala.getId(), responsavel, horarioInvalido);


        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Formato de horário inválido. Use HH:mm (ex: 14:00)", response.getBody());
    }


    @Test
    void deveRetornarConflitoQuandoTentaReservarComMenosDe30MinutosDeDiferenca() {
        StatusDaSala status = StatusDaSala.Disponivel;
        StatusDaSala statusOcupada = StatusDaSala.Ocupada;

        String responsavel = "teste1";
        String horarioAgendado = "13:00";
        String novoHorarioConflitante = "13:20";

        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        salasRepository.save(sala);

        Optional<Sala> findById = salasRepository.findById(sala.getId());


        SalaResevada salaResevada1 = new SalaResevada();
        salaResevada1.setSalaId(sala.getId());
        salaResevada1.setResponsavel_pela_sala(responsavel);
        salaResevada1.setCodigo_da_sala(sala.getCodigo());
        salaResevada1.setHorarioAgendado(horarioAgendado);
        salaResevada1.setStatus(statusOcupada);

        resevadaRepository.save(salaResevada1);

        rabbitTemplate.convertAndSend(RabbitMqConfig.SALARESERVADA, salaResevada1);

        List<SalaResevada> reservasExistentes = resevadaRepository.findBySalaId(sala.getId());
        ResponseEntity<?> response = service.reservaSala(sala.getId(), responsavel, novoHorarioConflitante);


        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertTrue(findById.isPresent());
        Assertions.assertEquals(1, reservasExistentes.size());
        Assertions.assertEquals("Já existe uma reserva próxima a esse horário. É necessário um intervalo de pelo menos 30 minutos.", response.getBody());
    }


    @Test
    void mostarSalasReservadas() {

        StatusDaSala status = StatusDaSala.Disponivel;
        StatusDaSala statusOcupada = StatusDaSala.Ocupada;

        String responsavel = "teste1";
        String horarioAgendado = "13:00";
        String novoHorario = "13:45";


        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        salasRepository.save(sala);

        Optional<Sala> findById = salasRepository.findById(sala.getId());


        SalaResevada salaResevada1 = new SalaResevada();
        salaResevada1.setSalaId(sala.getId());
        salaResevada1.setResponsavel_pela_sala(responsavel);
        salaResevada1.setCodigo_da_sala(sala.getCodigo());
        salaResevada1.setHorarioAgendado(horarioAgendado);
        salaResevada1.setStatus(statusOcupada);

        resevadaRepository.save(salaResevada1);

        SalaResevada salaResevada2 = new SalaResevada();
        salaResevada1.setSalaId(sala.getId());
        salaResevada1.setResponsavel_pela_sala(responsavel);
        salaResevada1.setCodigo_da_sala(sala.getCodigo());
        salaResevada1.setHorarioAgendado(novoHorario);
        salaResevada1.setStatus(statusOcupada);

        resevadaRepository.save(salaResevada2);


        List<SalaResevada> salaResevada = resevadaRepository.findAll();
        ResponseEntity<?> response = service.mostarSalasReservadas();


        Assertions.assertEquals(2, salaResevada.size());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(findById.isPresent());
    }


    @Test
    void nenhumaSalaReservada() {

        StatusDaSala status = StatusDaSala.Disponivel;

        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        salasRepository.save(sala);


        List<SalaResevada> salaResevada = resevadaRepository.findAll();


        ResponseEntity<?> isEmpty = service.mostarSalasReservadas();

        Assertions.assertTrue(salaResevada.isEmpty());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, isEmpty.getStatusCode());
        Assertions.assertEquals("Nenhuma sala reservada", isEmpty.getBody());
    }


    @Test
    void removerSalaPeloId() {
        StatusDaSala status = StatusDaSala.Disponivel;
        StatusDaSala statusOcupada = StatusDaSala.Ocupada;

        String responsavel = "teste1";
        String horarioAgendado = "13:00";
        String novoHorario = "13:45";


        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        salasRepository.save(sala);

        Optional<Sala> findById = salasRepository.findById(sala.getId());


        SalaResevada salaResevada1 = new SalaResevada();
        salaResevada1.setSalaId(sala.getId());
        salaResevada1.setResponsavel_pela_sala(responsavel);
        salaResevada1.setCodigo_da_sala(sala.getCodigo());
        salaResevada1.setHorarioAgendado(horarioAgendado);
        salaResevada1.setStatus(statusOcupada);

        resevadaRepository.save(salaResevada1);

        SalaResevada salaResevada2 = new SalaResevada();
        salaResevada1.setSalaId(sala.getId());
        salaResevada1.setResponsavel_pela_sala(responsavel);
        salaResevada1.setCodigo_da_sala(sala.getCodigo());
        salaResevada1.setHorarioAgendado(novoHorario);
        salaResevada1.setStatus(statusOcupada);

        resevadaRepository.save(salaResevada2);

        Assertions.assertTrue(findById.isPresent());

        ResponseEntity<?>response = service.removerSalaReservada(salaResevada1.getId());

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Sala removida com sucesso", response.getBody());
    }


}

