package com.helloboot.parkhanbeen.config.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import com.helloboot.parkhanbeen.config.ConditionalMyOnClass;
import com.helloboot.parkhanbeen.config.MyAutoConfiguration;

@MyAutoConfiguration
@ConditionalMyOnClass("org.eclipse.jetty.server.Server")
public class JettyWebserverConfig {

    @Bean("jettyWebServerFactory")
    @ConditionalOnMissingBean
    public ServletWebServerFactory serverFactory() {
        return new JettyServletWebServerFactory();
    }
}
