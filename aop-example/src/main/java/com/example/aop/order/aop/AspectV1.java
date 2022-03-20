package com.example.aop.order.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class AspectV1 {

  // 포인트 컷 표현식
  @Around("execution(* com.example.aop.order..*(..))")
  public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
    log.info("[log] {} ", joinPoint.getSignature());  //joinPoint 시그니처
    return joinPoint.proceed();
  }
}
