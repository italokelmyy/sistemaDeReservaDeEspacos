package com.github.com.italokelmyy.sistemaDeReservaDeEspaco;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SistemaDeReservaDeEspacoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaDeReservaDeEspacoApplication.class, args);


        Dotenv env = Dotenv.load();
        System.setProperty("JWT_SECRET_KEY", env.get("JWT_SECRET_KEY"));
        System.setProperty("SERVER_SSL_KEY_STORE_PASSWORD", env.get("SERVER_SSL_KEY_STORE_PASSWORD"));
        System.setProperty("SPRING_DATASOURCE_USERNAME", env.get("SPRING_DATASOURCE_USERNAME"));
        System.setProperty("SPRING_DATASOURCE_PASSWORD", env.get("SPRING_DATASOURCE_PASSWORD"));
    }

}
