package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.StatusDaSala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository.SalasRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalaService {

    private final SalasRepository repository;


    public SalaService(SalasRepository repository) {
        this.repository = repository;
    }

    public List<Sala> salasDisponiveis() {
        return repository.findAll();
    }


    public ResponseEntity<?> adicionarSala(Sala sala) {
        sala.setStatus(StatusDaSala.Disponivel);
        repository.save(sala);
        return ResponseEntity.ok("Sala adicionada com sucesso");
    }

    public ResponseEntity<?> removerSala(Long id) {
        repository.deleteById(id);
        return ResponseEntity.ok("Sala removida com sucesso");
    }


    public ResponseEntity<?> pesquisarPeloIdDaSala(Long id) {
        Optional<Sala> findById = repository.findById(id);

        if (findById.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sala não encontrada");
        }

        return ResponseEntity.ok(findById);
    }


}
