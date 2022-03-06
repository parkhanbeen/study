package com.example.advanced;

import com.example.advanced.trace.FieldLogTrace;
import com.example.advanced.trace.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogTraceConfig {

  @Bean
  public LogTrace logTrace() {
    return new FieldLogTrace();
  }
}
