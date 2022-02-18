package basics.thymeleaf.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import basics.thymeleaf.web.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String requestURI = request.getRequestURI();

    log.info("인증 체크 인터셉터 실행 {}", requestURI);

    HttpSession requestSession = request.getSession();

    if (requestSession == null || requestSession.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
      log.info("인증되지 않는 사용자 요청입니다.");
      response.sendRedirect("/login?redirectURL=" + requestURI);
      return false;
    }
    return true;
  }
}
