package com.example.aop.pointcut;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;

import com.example.aop.member.MemberService;
import com.example.aop.member.MemberServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

@Slf4j
public class ExecutionTest {

  AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
  Method method;

  @BeforeEach
  public void init() throws NoSuchMethodException {
    method = MemberServiceImpl.class.getMethod("hello", String.class);
  }

  @Test
  void printMethod() {
    log.info("helloMethod = {}", method);
  }

  @Test
  void exactMatch() {
    pointcut.setExpression("execution(public String com.example.aop.member.MemberServiceImpl.hello(String))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void allMatch() {
    pointcut.setExpression("execution(* *(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void nameMatch() {
    pointcut.setExpression("execution(* hello(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void nameStar1() {
    pointcut.setExpression("execution(* hell*(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void nameStarFalse() {
    pointcut.setExpression("execution(* not(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isFalse();
  }

  @Test
  void packageExactMatch1() {
    pointcut.setExpression("execution(* com.example.aop.member.MemberServiceImpl.hello(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void packageMatchSubPackage1() {
    pointcut.setExpression("execution(* com.example.aop.member..*(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void packageMatchSubPackage2() {
    pointcut.setExpression("execution(* com.example.aop..*(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void typeExactMatch() {
    pointcut.setExpression("execution(* com.example.aop.member.MemberServiceImpl.*(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void typeExactMatchSuperType() {
    pointcut.setExpression("execution(* com.example.aop.member.MemberService.*(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void typeExactMatchInternal() throws NoSuchMethodException {
    pointcut.setExpression("execution(* com.example.aop.member.MemberServiceImpl.*(..))");
    Method internalMethod = MemberServiceImpl.class.getMethod("internal", String.class);
    assertThat(pointcut.matches(internalMethod, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void typeExactMatchNoSuperTypeMethodFalse() throws NoSuchMethodException {
    pointcut.setExpression("execution(* com.example.aop.member.MemberService.*(..))");
    Method internalMethod = MemberServiceImpl.class.getMethod("internal", String.class);
    assertThat(pointcut.matches(internalMethod, MemberServiceImpl.class)).isFalse();
  }

  @Test
  void argsMatch() {
    pointcut.setExpression("execution(* *(String))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void argsMatchNoArgs() {
    pointcut.setExpression("execution(* *())");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isFalse();
  }

  @Test
  void argsMatchStar() {
    pointcut.setExpression("execution(* *(*))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void argsMatchAll() {
    pointcut.setExpression("execution(* *(..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

  @Test
  void argsMatchComplex() {
    pointcut.setExpression("execution(* *(String, ..))");
    assertThat(pointcut.matches(method, MemberServiceImpl.class)).isTrue();
  }

}
