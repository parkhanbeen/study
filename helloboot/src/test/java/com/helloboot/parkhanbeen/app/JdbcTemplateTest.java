package com.helloboot.parkhanbeen.app;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
class JdbcTemplateTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        jdbcTemplate.execute("create table if not exists member(name varchar(50) primary key, count int)");
    }

    @Test
    void insertAndQuery() {
        jdbcTemplate.update("insert into member values(?, ?)", "hanbeen", 3);
        jdbcTemplate.update("insert into member values(?, ?)", "spring", 1);

        Long count = jdbcTemplate.queryForObject("select count(*) from member", Long.class);
        assertThat(count).isEqualTo(2);
    }
}
