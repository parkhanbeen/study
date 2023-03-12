package com.helloboot.parkhanbeen.app;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class MemberRepositoryTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MemberRepository memberRepository;

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
