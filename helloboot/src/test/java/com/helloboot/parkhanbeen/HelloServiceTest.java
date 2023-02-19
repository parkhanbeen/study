package com.helloboot.parkhanbeen;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelloServiceTest {
    @Test
    void simpleHelloService() {
        SimpleHelloService simpleHelloService = new SimpleHelloService();

        String result = simpleHelloService.sayHello("Test");

        Assertions.assertThat(result).isEqualTo("Hello Test");
    }

    @Test
    void helloDecorator() {
        HelloDecorator decorator = new HelloDecorator(name -> name);

        String result = decorator.sayHello("Test");

        Assertions.assertThat(result).isEqualTo("*Test*");
    }
}