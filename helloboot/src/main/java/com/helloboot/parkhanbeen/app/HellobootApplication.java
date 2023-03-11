package com.helloboot.parkhanbeen.app;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import com.helloboot.parkhanbeen.config.MySpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

@MySpringBootApplication
public class HellobootApplication {

    private final JdbcTemplate jdbcTemplate;

    public HellobootApplication(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void init() {
        jdbcTemplate.execute("create table if not exists member(name varchar(50) primary key, count int)");
    }

    public static void main(String[] args) {
        SpringApplication.run(HellobootApplication.class, args);
    }

}
