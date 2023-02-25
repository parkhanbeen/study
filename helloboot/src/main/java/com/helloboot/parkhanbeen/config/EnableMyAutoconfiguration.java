package com.helloboot.parkhanbeen.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import com.helloboot.parkhanbeen.config.autoconfig.DispatcherServletConfig;
import com.helloboot.parkhanbeen.config.autoconfig.TomcatWebserverConfig;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({DispatcherServletConfig.class, TomcatWebserverConfig.class})
public @interface EnableMyAutoconfiguration {

}
