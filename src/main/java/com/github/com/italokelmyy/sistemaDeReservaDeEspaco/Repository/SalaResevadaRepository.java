package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Repository;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.SalaResevada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SalaResevadaRepository extends JpaRepository<SalaResevada, Long> {



    List<SalaResevada> findBySalaId(Long salaId);
}