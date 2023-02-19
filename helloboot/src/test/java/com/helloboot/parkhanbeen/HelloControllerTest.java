package com.helloboot.parkhanbeen;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class HelloControllerTest {

    @Test
    void helloController() {
        HelloController helloController = new HelloController(name -> name);

        String result = helloController.hello("hanbeen");

        Assertions.assertThat(result).isEqualTo("hanbeen");
    }

    @Test
    void fails_helloController() {
        HelloController helloController = new HelloController(name -> name);

        Assertions.assertThatThrownBy(() -> {
            helloController.hello(null);
        }).isInstanceOf(IllegalArgumentException.class);

        Assertions.assertThatThrownBy(() -> {
            helloController.hello("");
        }).isInstanceOf(IllegalArgumentException.class);

    }
}
