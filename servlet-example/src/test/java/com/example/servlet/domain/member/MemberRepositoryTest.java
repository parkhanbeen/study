package com.example.servlet.domain.member;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MemberRepositoryTest {

  MemberRepository memberRepository = MemberRepository.getInstance();

  @AfterEach
  public void after() {
    memberRepository.clearStore();
  }

  @Test
  void save() {
    // given
    Member givenMember = new Member("park", 30);

    // when
    Member savedMember = memberRepository.save(givenMember);

    // then
    Member findMember = memberRepository.findById(savedMember.getId());
    assertThat(findMember).isEqualTo(savedMember);

  }

  @Test
  void findAll() {
    // given
    Member givenMember1 = new Member("park", 30);
    Member givenMember2 = new Member("son", 27);

    memberRepository.save(givenMember1);
    memberRepository.save(givenMember2);

    // when
    List<Member> result = memberRepository.findAll();

    // then
    assertThat(result.size()).isEqualTo(2);
    assertThat(result).contains(givenMember1, givenMember2);
  }

}
