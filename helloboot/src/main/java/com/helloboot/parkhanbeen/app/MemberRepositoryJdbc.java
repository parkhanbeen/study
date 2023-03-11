package com.helloboot.parkhanbeen.app;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepositoryJdbc implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemberRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Member findMember(String name) {

        try {
            return jdbcTemplate.queryForObject("select * from member where name = '" + name + "'",
                (rs, rowNum) -> new Member(
                    rs.getString("name"), rs.getInt("count"))
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void increase(String name) {
        Member member = findMember(name);
        if (member == null) {
            jdbcTemplate.update("insert into member values (?, ?)",
                name,
                1);

        } else {
            jdbcTemplate.update("update member set count = ? where name = ?",
                member.getCount() + 1,
                name);
        }

    }
}
