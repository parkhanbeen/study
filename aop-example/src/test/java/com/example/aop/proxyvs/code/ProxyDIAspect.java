package com.example.aop.proxyvs.code;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Slf4j
@Aspect
public class ProxyDIAspect {

  @Before("execution(* com.example.aop..*.*(..))")
  public void doTrace(JoinPoint joinPoint) {
    log.info("ProxyDIAdvice = {}", joinPoint.getSignature());
  }
}
