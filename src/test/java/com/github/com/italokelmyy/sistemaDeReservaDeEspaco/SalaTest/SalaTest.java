package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.SalaTest;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.StatusDaSala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalasRepository;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.SalaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SalaTest {

    @Mock
    private SalasRepository repository;

    @InjectMocks
    private SalaService service;


    @BeforeEach
    void testDown() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void test_mostrar_todas_as_salas_disponiveis() {
        Sala sala1 = new Sala(1L, "Desenvolvedor de Software", "sala-15", 25L, "Segundo andar");
        Sala sala2 = new Sala(2L, "Desenvolvedor de Mobile", "sala-20", 28L, "Segundo andar");

        Mockito.when(repository.findAll()).thenReturn(Arrays.asList(sala1, sala2));


        List<Sala> salaList = service.salasDisponiveis();


        Assertions.assertEquals(2, salaList.size());

        Mockito.verify(repository, Mockito.times(1)).findAll();
    }

    @Test
    void test_adicionar_sala_para_reuniao() {
        Sala sala1 = new Sala(1L, "Comemoração do ano novo", "sala-15", 25L, "Segundo andar");
        StatusDaSala statusDaSala = StatusDaSala.Disponivel;
        sala1.setStatus(statusDaSala);

        Mockito.when(repository.save(Mockito.any(Sala.class))).thenReturn(sala1);

        ResponseEntity<?> response = service.adicionarSala(sala1);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Sala adicionada com sucesso", response.getBody());

        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Sala.class));
    }

    @Test
    void test_remover_sala() {
        Long id = 1L;

        ResponseEntity<?> response = service.removerSala(id);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Sala removida com sucesso", response.getBody());

        Mockito.verify(repository, Mockito.times(1)).deleteById(id);
    }

    @Test
    void test_pesquisar_pelo_id_da_sala() {
        Long id = 1L;
        Sala sala1 = new Sala(id, "Desenvolvedor de Software", "sala-15", 25L, "Segundo andar");
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(sala1));

        ResponseEntity<?> response = service.pesquisarPeloIdDaSala(id);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());

        Mockito.verify(repository, Mockito.times(1)).findById(id);
    }

    @Test
    void test_erro_para_sala_nao_encontrada_pelo_id() {
        Long id = 24L;

        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.pesquisarPeloIdDaSala(id);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals("Sala não encontrada", response.getBody());
        Mockito.verify(repository, Mockito.times(1)).findById(id);
    }


}
