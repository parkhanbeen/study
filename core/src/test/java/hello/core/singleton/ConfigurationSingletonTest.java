package hello.core.singleton;

import hello.core.AppConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ConfigurationSingletonTest {

    @Test
    void configurationDeep() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        AppConfig bean = ac.getBean(AppConfig.class);

        System.out.println("bean = " + bean.getClass());
        // bean = class hello.core.AppConfig$$EnhancerBySpringCGLIB$$72a6cf03
        // 스프링이 cglib라는 바이트 코드 조작 라이브러리를 통해 임의의 다른 클래스를 만들고 스프링 빈으로 등록한다.
    }
}
