package basics.thymeleaf.web.session;

import static org.assertj.core.api.Assertions.assertThat;

import basics.thymeleaf.domain.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SessionManagerTest {

  SessionManager sessionManager = new SessionManager();

  @Test
  void sessionTest() {

    MockHttpServletResponse response = new MockHttpServletResponse();

    Member member = new Member();
    sessionManager.createSession(member, response);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(response.getCookies());

    Object result = sessionManager.getSession(request);
    assertThat(result).isEqualTo(member);

    sessionManager.expire(request);
    Object expired = sessionManager.getSession(request);
    assertThat(expired).isNull();
  }

}
