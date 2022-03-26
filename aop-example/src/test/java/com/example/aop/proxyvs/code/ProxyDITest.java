package com.example.aop.proxyvs.code;

import com.example.aop.member.MemberService;
import com.example.aop.member.MemberServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
//@SpringBootTest(properties = {"spring.aop.proxy-target-class=false"})  // jdk 동적 프록시
@SpringBootTest(properties = {"spring.aop.proxy-target-class=true"})  // cglib 프록시
@Import(ProxyDIAspect.class)
public class ProxyDITest {

  @Autowired
  MemberService memberService;

  @Autowired
  MemberServiceImpl memberServiceImpl;

  @Test
  void go() {
    log.info("memberService class= {}", memberService.getClass());
    log.info("memberServiceImpl class= {}", memberServiceImpl.getClass());
    memberService.hello("hello");
  }
}
