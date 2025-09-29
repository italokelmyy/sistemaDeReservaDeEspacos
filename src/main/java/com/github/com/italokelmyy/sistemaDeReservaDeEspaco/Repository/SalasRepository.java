package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.Sala;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.StatusDaSala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalasRepository extends JpaRepository<Sala, Long> {

    List<Sala> findByStatus(StatusDaSala status);

}
