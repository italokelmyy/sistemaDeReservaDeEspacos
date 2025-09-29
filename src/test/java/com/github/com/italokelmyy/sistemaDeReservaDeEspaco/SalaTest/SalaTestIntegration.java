package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.SalaTest;


import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.StatusDaSala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalasRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.SalaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
public class SalaTestIntegration {

    @Autowired
    private SalasRepository repository;

    @Autowired
    private SalaService salaService;


    @AfterEach
    void test() {
        repository.deleteAll();
    }


    @Test
    void mostrarSalaDisponivel() {

        StatusDaSala status = StatusDaSala.Disponivel;

        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        repository.save(sala);


        List<Sala> salaList = salaService.salasDisponiveis();

        Assertions.assertEquals(1, salaList.size());
    }

    @Test
    void adicionarSala() {
        StatusDaSala status = StatusDaSala.Disponivel;

        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        repository.save(sala);

        ResponseEntity<?> adicionar = salaService.adicionarSala(sala);

        Assertions.assertEquals(HttpStatus.OK, adicionar.getStatusCode());
        Assertions.assertEquals("Sala adicionada com sucesso", adicionar.getBody());
    }


    @Test
    void removerSalaPeloId() {

        StatusDaSala status = StatusDaSala.Disponivel;

        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        repository.save(sala);

        repository.deleteById(sala.getId());

        ResponseEntity<?> remov = salaService.removerSala(sala.getId());

        Assertions.assertEquals(HttpStatus.OK, remov.getStatusCode());
        Assertions.assertEquals("Sala removida com sucesso", remov.getBody());
    }


    @Test
    void pesquisarSalaPeloId() {
        StatusDaSala status = StatusDaSala.Disponivel;

        Sala sala = new Sala();
        sala.setArea("Tecnologia");
        sala.setCodigo("sala-10");
        sala.setCapacidade(23L);
        sala.setLocalizacao("Primeiro andar");
        sala.setStatus(status);

        repository.save(sala);

        Optional<Sala> findById = repository.findById(sala.getId());

        ResponseEntity<?> response = salaService.pesquisarPeloIdDaSala(sala.getId());

        Assertions.assertNotNull(response);
        Assertions.assertTrue(findById.isPresent());
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
    }




}
