package com.helloboot.parkhanbeen.study;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigurationTest {
    @Test
    void configuration() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(MyConfig.class);
        context.refresh();

        Bean1 bean1 = context.getBean(Bean1.class);
        Bean2 bean2 = context.getBean(Bean2.class);

        Assertions.assertThat(bean1.common).isSameAs(bean2.common);
    }


    @Test
    void proxyCommonMethod() {
        MyConfigProxy myConfigProxy = new MyConfigProxy();

        Bean1 bean1 = myConfigProxy.bean1();
        Bean2 bean2 = myConfigProxy.bean2();

        Assertions.assertThat(bean1.common).isSameAs(bean2.common);
    }


    static class MyConfigProxy extends MyConfig {
        private Common common;
        @Override
        Common common() {
            if (this.common == null) {
                this.common = super.common();
            }
            return this.common;
        }
    }


    @Configuration
    static class MyConfig {
        @Bean
        Common common() {
            return new Common();
        }

        @Bean
        Bean1 bean1() {
            return new Bean1(common());
        }

        @Bean
        Bean2 bean2() {
            return new Bean2(common());
        }
    }

    static class Bean1 {
        private final Common common;

        public Bean1(Common common) {
            this.common = common;
        }
    }

    static class Bean2 {
        private final Common common;

        public Bean2(Common common) {
            this.common = common;
        }
    }

    static class Common {

    }


}
