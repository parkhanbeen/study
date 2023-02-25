package com.helloboot.parkhanbeen.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Configuration(proxyBeanMethods = false)  // proxyBeanMethods = false 옵션일 경우 프록시로 bean을 생성하지 않음
public @interface MyAutoConfiguration {

}
