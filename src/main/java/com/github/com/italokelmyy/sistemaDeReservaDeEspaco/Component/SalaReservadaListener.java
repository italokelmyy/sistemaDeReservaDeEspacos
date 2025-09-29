package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Component;

import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Configuration.RabbitMqConfig;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Entity.SalaResevada;
import com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Service.SalaReservadaService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SalaReservadaListener {



    @RabbitListener(queues = RabbitMqConfig.SALARESERVADA)
    public void receberSalaReservada(SalaResevada reservadaDTO) {
        System.out.println("Mensagem recebida para a reserva da sala: " + reservadaDTO.getCodigo_da_sala());
    }

}
