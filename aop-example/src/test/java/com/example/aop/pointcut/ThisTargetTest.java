package com.example.aop.pointcut;

import com.example.aop.member.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Slf4j
@Import(ThisTargetTest.ThisTargetAspect.class)
//@SpringBootTest(properties = "spring.aop.proxy-target-class=false")  // JDK 동적 프록시 설정
@SpringBootTest(properties = "spring.aop.proxy-target-class=true")  // CGLIB 프록시 설정 default
public class ThisTargetTest {

  @Autowired
  MemberService memberService;

  @Test
  void success() {
    log.info("memberService proxy = {}", memberService.getClass());
    memberService.hello("hello");
  }

  @Slf4j
  @Aspect
  static class ThisTargetAspect {

    @Around("this(com.example.aop.member.MemberService)")
    public Object doThisInterface(ProceedingJoinPoint joinPoint) throws Throwable {

      log.info("[this-interface] {}", joinPoint.getSignature());

      return joinPoint.proceed();
    }

    @Around("target(com.example.aop.member.MemberService)")
    public Object doTargetInterface(ProceedingJoinPoint joinPoint) throws Throwable {

      log.info("[target-interface] {}", joinPoint.getSignature());

      return joinPoint.proceed();
    }

    @Around("this(com.example.aop.member.MemberServiceImpl)")
    public Object doThis(ProceedingJoinPoint joinPoint) throws Throwable {

      log.info("[this] {}", joinPoint.getSignature());

      return joinPoint.proceed();
    }

    @Around("target(com.example.aop.member.MemberServiceImpl)")
    public Object doTarget(ProceedingJoinPoint joinPoint) throws Throwable {

      log.info("[target] {}", joinPoint.getSignature());

      return joinPoint.proceed();
    }

  }

}
