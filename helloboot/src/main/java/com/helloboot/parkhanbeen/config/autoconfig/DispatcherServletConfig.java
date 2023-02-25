package com.helloboot.parkhanbeen.config.autoconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import com.helloboot.parkhanbeen.config.MyAutoConfiguration;

@MyAutoConfiguration
public class DispatcherServletConfig {

    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

}
