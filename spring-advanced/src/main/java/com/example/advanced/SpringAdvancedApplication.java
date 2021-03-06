package com.example.advanced;

import com.example.advanced.proxy.config.AppV1Config;
import com.example.advanced.proxy.config.AppV2Config;
import com.example.advanced.proxy.config.BeanPostProcessorConfig;
import com.example.advanced.proxy.config.v1proxy.ConcreteProxyConfig;
import com.example.advanced.proxy.config.v1proxy.InterfaceProxyConfig;
import com.example.advanced.proxy.config.v2dynamicproxy.DynamicProxyBasicConfig;
import com.example.advanced.proxy.config.v2dynamicproxy.DynamicProxyFilterConfig;
import com.example.advanced.proxy.config.v3proxyfactory.ProxyFactoryConfigV1;
import com.example.advanced.proxy.config.v3proxyfactory.ProxyFactoryConfigV2;
import com.example.advanced.proxy.config.v5autoproxy.AutoProxyConfig;
import com.example.advanced.proxy.config.v6aop.AopConfig;
import com.example.advanced.trace.LogTrace;
import com.example.advanced.trace.ThreadLocalLogTrace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

//@Import({AppV1Config.class, AppV2Config.class})
//@Import(InterfaceProxyConfig.class)
//@Import(ConcreteProxyConfig.class)
//@Import(DynamicProxyBasicConfig.class)
//@Import(DynamicProxyFilterConfig.class)
//@Import(ProxyFactoryConfigV1.class)
//@Import(ProxyFactoryConfigV2.class)
//@Import(BeanPostProcessorConfig.class)
//@Import(AutoProxyConfig.class)
@Import(AopConfig.class)
@SpringBootApplication(scanBasePackages = "com.example.advanced.proxy.app")
public class SpringAdvancedApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringAdvancedApplication.class, args);
  }

  @Bean
  public LogTrace logTrace() {
    return new ThreadLocalLogTrace();
  }

}
