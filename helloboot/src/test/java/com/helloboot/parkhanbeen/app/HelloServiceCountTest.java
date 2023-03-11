package com.helloboot.parkhanbeen.app;

import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@HelloBootTest
class HelloServiceCountTest {
    @Autowired
    HelloService helloService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void sayHelloIncreaseCount() {
        IntStream.rangeClosed(1, 10).forEach(count -> {
            helloService.sayHello("hanbeen");
            Assertions.assertThat(memberRepository.countOf("hanbeen")).isEqualTo(count);
        });
    }

}
