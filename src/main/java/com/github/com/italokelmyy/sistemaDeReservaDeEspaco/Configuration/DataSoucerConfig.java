package com.github.com.italokelmyy.sistemaDeReservaDeEspaco.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


import javax.sql.DataSource;

@Profile("dev")
@Configuration
public class DataSoucerConfig {

    @Value("${SPRING_DATASOURCE_USERNAME}")
    private String usuario;
    @Value("${SPRING_DATASOURCE_PASSWORD}")
    private String senha;


    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://localhost:3306/sistemareservasdeespacos?useSSL=false&serverTimezone=UTC")
                .username(usuario)
                .password(senha)
                .build();
    }

}
