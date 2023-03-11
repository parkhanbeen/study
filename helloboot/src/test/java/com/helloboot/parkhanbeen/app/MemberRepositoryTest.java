package com.helloboot.parkhanbeen.app;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@HelloBootTest
class MemberRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MemberRepository memberRepository;

//    @BeforeEach
//    void init() {
//        jdbcTemplate.execute("create table if not exists member(name varchar(50) primary key, count int)");
//    }

    @Test
    void findMemberFailed() {
        assertThat(memberRepository.findMember("hanbeen")).isNull();
    }

    @Test
    void increaseCount() {

        assertThat(memberRepository.countOf("hanbeen")).isEqualTo(0);

        memberRepository.increase("hanbeen");
        assertThat(memberRepository.countOf("hanbeen")).isEqualTo(1);

        memberRepository.increase("hanbeen");
        assertThat(memberRepository.countOf("hanbeen")).isEqualTo(2);
    }
}
