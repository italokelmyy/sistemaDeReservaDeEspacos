package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Controller;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sala")
public class SalasController {

    private final SalaService salaService;


    @Autowired
    public SalasController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping("/lista")
    public List<Sala> salaList() {
        return salaService.salasDisponiveis();
    }

    @PostMapping("/adicionarSala")
    public ResponseEntity<?> adicionar(@RequestBody Sala sala) {
        return salaService.adicionarSala(sala);
    }

    @GetMapping("/removerSala/{id}")
    public ResponseEntity<?> removerById(@PathVariable Long id) {
        return salaService.removerSala(id);
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<?> procurarPeloId(@PathVariable Long id) {
        return salaService.pesquisarPeloIdDaSala(id);
    }


}
