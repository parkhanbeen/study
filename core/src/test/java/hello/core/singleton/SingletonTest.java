package hello.core.singleton;

import hello.core.AppConfig;
import hello.core.member.MemberService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SingletonTest {

    @Test
    @DisplayName("순수한 DI 컨테이너")
    void pureContainer() {
        AppConfig appConfig = new AppConfig();
        // 1. 조회: 호출 할때 마다 객체 생성.
        MemberService memberService1 = appConfig.memberService();

        // 2. 조회: 호출 할때 마다 객체 생성.
        MemberService memberService2 = appConfig.memberService();

        // 3. 참조 값이 다른 것을 확인
        Assertions.assertThat(memberService1).isNotSameAs(memberService2);

    }
}
