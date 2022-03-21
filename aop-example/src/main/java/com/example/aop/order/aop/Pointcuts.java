package com.example.aop.order.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {

  @Pointcut("execution(* com.example.aop.order..*(..))")
  public void allOrder() {}

  // 클래스 명 패턴 *Service
  @Pointcut("execution(* *..*Service.*(..))")
  public void allService() {}

  @Pointcut("allOrder() && allService()")
  public void orderAndService() {}
}
