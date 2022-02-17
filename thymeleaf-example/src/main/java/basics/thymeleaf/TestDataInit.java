package basics.thymeleaf;

import basics.thymeleaf.domain.item.Item;
import basics.thymeleaf.domain.item.ItemRepository;
import javax.annotation.PostConstruct;

import basics.thymeleaf.domain.member.Member;
import basics.thymeleaf.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;

    /**
     * 테스트용 데이터 추가
     */
    @PostConstruct
    public void init() {
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemB", 20000, 20));

        Member member = new Member();
        member.setLoginId("park");
        member.setPassword("park!");
        member.setName("parkhanbeen");

        memberRepository.save(member);
    }

}
