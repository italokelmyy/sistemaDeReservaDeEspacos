package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.SalaReservadaTest;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Configuration.RabbitMqConfig;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.SalaResevada;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.StatusDaSala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalaResevadaRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalasRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.SalaReservadaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SalaReservarTest {
    @Mock
    private SalaResevadaRepository resevadaRepository;
    @Mock
    SalasRepository salasRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SalaReservadaService service;

    @BeforeEach
    void test_down() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void test_reservar_sala() {

        Long id = 1L;
        String responsavel = "teste1";
        String horarioAgendado = "13:00";
        StatusDaSala status = StatusDaSala.Ocupada;

        Sala sala = new Sala();
        sala.setId(id);
        sala.setArea("Desenvolvedor de Software");
        sala.setCodigo("sala-15");
        sala.setCapacidade(25L);
        sala.setLocalizacao("Segundo andar");


        Mockito.when(salasRepository.save(Mockito.any(Sala.class))).thenReturn(sala);
        Mockito.when(salasRepository.findById(id)).thenReturn(Optional.of(sala));


        SalaResevada salaResevada = new SalaResevada();
        salaResevada.setSalaId(sala.getId());
        salaResevada.setResponsavel_pela_sala(responsavel);
        salaResevada.setCodigo_da_sala(sala.getCodigo());
        salaResevada.setHorarioAgendado(horarioAgendado);
        salaResevada.setStatus(status);

        Mockito.when(resevadaRepository.save(Mockito.any(SalaResevada.class))).thenAnswer(invocation -> {
            SalaResevada sr = invocation.getArgument(0);
            sr.setId(id);
            return sr;
        });
        Mockito.when(resevadaRepository.findBySalaId(id)).thenReturn(Collections.emptyList());


        rabbitTemplate.convertAndSend(RabbitMqConfig.SALARESERVADA, salaResevada);
        ResponseEntity<?> response = service.reservaSala(id, salaResevada.getResponsavel_pela_sala(), salaResevada.getHorarioAgendado());

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Sala adicionada com sucesso", response.getBody());


        Mockito.verify(salasRepository, Mockito.times(1)).findById(id);
        Mockito.verify(resevadaRepository, Mockito.times(1)).save(Mockito.any(SalaResevada.class));
    }

    @Test
    void testAoTentarSalvarUmaSalaPeloIdQueNaoExiste() {
        Long id = 4L;
        String responsavel = "teste1";
        String horarioAgendado = "13:00";

        Sala sala = new Sala();

        sala.setId(1L);
        sala.setArea("Desenvolvedor de Software");
        sala.setCodigo("sala-15");
        sala.setCapacidade(25L);
        sala.setLocalizacao("Segundo andar");

        Mockito.when(salasRepository.save(Mockito.any(Sala.class))).thenReturn(sala);
        Mockito.when(resevadaRepository.findBySalaId(id)).thenReturn(Collections.emptyList());


        ResponseEntity<?> response = service.reservaSala(id, responsavel, horarioAgendado);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals("Sala não encontrada", response.getBody());

    }

    @Test
    void deveRetornarBadRequestParaFormatoDeHorarioInvalido() {

        Long salaId = 1L;
        String responsavel = "Usuario Teste";
        String horarioInvalido = "14-30";

        Sala salaMock = new Sala();
        salaMock.setId(salaId);
        salaMock.setCodigo("sala-01");


        Mockito.when(salasRepository.findById(salaId)).thenReturn(Optional.of(salaMock));


        ResponseEntity<?> response = service.reservaSala(salaId, responsavel, horarioInvalido);


        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Formato de horário inválido. Use HH:mm (ex: 14:00)", response.getBody());


        Mockito.verify(resevadaRepository, Mockito.never()).save(Mockito.any(SalaResevada.class));
    }


    @Test
    void mostarSalasReservadas() {

        Long id = 1L;
        String responsavel1 = "teste1";
        String horarioAgendado = "13:00";
        StatusDaSala status = StatusDaSala.Ocupada;


        Long id2 = 2L;
        String responsavel2 = "teste1";
        String horarioAgendado2 = "13:00";
        StatusDaSala status2 = StatusDaSala.Ocupada;


        SalaResevada salaResevada1 = new SalaResevada();
        salaResevada1.setSalaId(id);
        salaResevada1.setResponsavel_pela_sala(responsavel1);
        salaResevada1.setCodigo_da_sala("sala-01");
        salaResevada1.setHorarioAgendado(horarioAgendado);
        salaResevada1.setStatus(status);


        SalaResevada salaResevada2 = new SalaResevada();
        salaResevada2.setSalaId(id2);
        salaResevada2.setResponsavel_pela_sala(responsavel2);
        salaResevada2.setCodigo_da_sala("sala-02");
        salaResevada2.setHorarioAgendado(horarioAgendado2);
        salaResevada2.setStatus(status2);

        Mockito.when(resevadaRepository.findAll()).thenReturn(Arrays.asList(salaResevada1, salaResevada2));

        List<SalaResevada> salaResevadaList = Arrays.asList(salaResevada1, salaResevada2);


        ResponseEntity<?> response = service.mostarSalasReservadas();


        Assertions.assertNotNull(salaResevadaList);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(2, salaResevadaList.size());

        Mockito.verify(resevadaRepository, Mockito.times(1)).findAll();
    }

    @Test
    void removerSalaReservada() {

        Long id = 1L;
        String responsavel1 = "teste1";
        String horarioAgendado = "13:00";
        StatusDaSala status = StatusDaSala.Ocupada;


        SalaResevada salaResevada1 = new SalaResevada();
        salaResevada1.setSalaId(id);
        salaResevada1.setResponsavel_pela_sala(responsavel1);
        salaResevada1.setCodigo_da_sala("sala-01");
        salaResevada1.setHorarioAgendado(horarioAgendado);
        salaResevada1.setStatus(status);



        ResponseEntity<?> deleteById = service.removerSalaReservada(salaResevada1.getId());

        Assertions.assertEquals(HttpStatus.OK, deleteById.getStatusCode());
        Assertions.assertEquals("Sala removida com sucesso", deleteById.getBody());

        Mockito.verify(resevadaRepository, Mockito.times(1)).deleteById(salaResevada1.getId());
    }


    @Test
    void deveRetornarConflitoQuandoTentaReservarComMenosDe30MinutosDeDiferenca() {

        Long salaId = 1L;
        String responsavel = "Usuario Teste";
        String horarioExistente = "14:00";
        String novoHorarioConflitante = "14:29";


        Sala salaMock = new Sala();
        salaMock.setId(salaId);
        salaMock.setCodigo("sala-01");


        SalaResevada reservaExistente = new SalaResevada();
        reservaExistente.setHorarioAgendado(horarioExistente);
        reservaExistente.setSalaId(salaId);


        List<SalaResevada> reservasExistentes = Collections.singletonList(reservaExistente);


        Mockito.when(salasRepository.findById(salaId)).thenReturn(Optional.of(salaMock));
        Mockito.when(resevadaRepository.findBySalaId(salaId)).thenReturn(reservasExistentes);


        ResponseEntity<?> response = service.reservaSala(salaId, responsavel, novoHorarioConflitante);


        Assertions.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        Assertions.assertEquals("Já existe uma reserva próxima a esse horário. É necessário um intervalo de pelo menos 30 minutos.", response.getBody());


        Mockito.verify(resevadaRepository, Mockito.never()).save(Mockito.any(SalaResevada.class));
    }



}
