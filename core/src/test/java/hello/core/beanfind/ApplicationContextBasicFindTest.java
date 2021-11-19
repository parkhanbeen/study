package hello.core.beanfind;

import hello.core.AppConfig;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationContextBasicFindTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("이름으로 조회")
    void findByBeanName() {
        MemberService memberService = ac.getBean("memberService", MemberService.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);


    }

    @Test
    @DisplayName("타입으로 조회")
    void findByBeanType() {
        MemberService memberService = ac.getBean(MemberService.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("구현체 타입으로 조회")
    void findByImplType() {

        // 구현으로 찾는 경우는 좋지 않음 역할에 의존해야지 구현에 의존하면 안되기 때문이다.
        // 이렇게 찾을 수도 있다 라는 경우의 예시.
        MemberService memberService = ac.getBean(MemberServiceImpl.class);
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("이름으로 조회가 안될 경우 NoSuchBeanDefinitionException가 발생한다.")
    void findByBeanNameFail() {
        assertThrows(NoSuchBeanDefinitionException.class,
                () ->  ac.getBean("productService", MemberService.class));
    }
}
