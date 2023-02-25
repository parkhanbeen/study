package com.helloboot.parkhanbeen.config.autoconfig;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import com.helloboot.parkhanbeen.config.MyAutoConfiguration;

@MyAutoConfiguration
public class TomcatWebserverConfig {

    @Bean
    public ServletWebServerFactory serverFactory() {
        return new TomcatServletWebServerFactory();
    }
}
