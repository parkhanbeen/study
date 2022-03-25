package com.example.aop.example.aop;

import com.example.aop.example.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class RetryAspect {

  @Around("@annotation(retry)")
  public Object doRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {

    log.info("[retry] {}, retry = {}", joinPoint.getSignature(), retry);
    int maxRetry = retry.value();

    Exception exceptionHolder = null;

    for (int retryCount = 1; retryCount <= maxRetry; retryCount++) {
      try {
        log.info("[retry] try count = {}/{}", retryCount, maxRetry);
        return joinPoint.proceed();
      } catch (Exception e) {
        exceptionHolder = e;
      }
    }
    return exceptionHolder;
  }
}
