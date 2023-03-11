package com.helloboot.parkhanbeen.app;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class HelloServiceTest {
    @Test
    void simpleHelloService() {
        SimpleHelloService simpleHelloService = new SimpleHelloService(new MemberRepository() {
            @Override
            public Member findMember(String name) {
                return null;
            }

            @Override
            public void increase(String name) {
            }
        });

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
