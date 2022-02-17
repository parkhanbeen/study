package basics.thymeleaf.domain.login;

import basics.thymeleaf.domain.member.Member;
import basics.thymeleaf.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginService {

  private final MemberRepository memberRepository;

  public Member login(String loginId, String password) {
    return memberRepository.findByLoginId(loginId)
        .filter(member -> member.getPassword().equals(password))
        .orElse(null);
  }
}
