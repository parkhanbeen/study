package com.example.advanced.proxy.config.v6aop;

import com.example.advanced.proxy.config.AppV1Config;
import com.example.advanced.proxy.config.AppV2Config;
import com.example.advanced.proxy.config.v6aop.aspect.LogTraceAspect;
import com.example.advanced.trace.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AppV1Config.class, AppV2Config.class})
public class AopConfig {

  @Bean
  public LogTraceAspect logTraceAspect(LogTrace logTrace) {
    return new LogTraceAspect(logTrace);
  }

}
