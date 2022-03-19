package com.example.advanced.proxy.config;

import com.example.advanced.proxy.config.v3proxyfactory.advice.LogTraceAdvice;
import com.example.advanced.proxy.config.v4postprocessor.postprocessor.PackageLogTracePostProcessor;
import com.example.advanced.trace.LogTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import({AppV1Config.class, AppV2Config.class})
public class BeanPostProcessorConfig {

  @Bean
  public PackageLogTracePostProcessor logTracePostProcessor(LogTrace logTrace) {
    return new PackageLogTracePostProcessor(
        "com.example.advanced.proxy.app",
        getAdvisor(logTrace));
  }

  private Advisor getAdvisor(LogTrace logTrace) {
    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
    pointcut.setMappedNames("request*", "order*", "save*");

    // advice
    LogTraceAdvice advice = new LogTraceAdvice(logTrace);

    return new DefaultPointcutAdvisor(pointcut, advice);
  }

}
