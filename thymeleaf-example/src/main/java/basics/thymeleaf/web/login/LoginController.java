package basics.thymeleaf.web.login;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import basics.thymeleaf.domain.login.LoginService;
import basics.thymeleaf.domain.member.Member;
import basics.thymeleaf.web.SessionConst;
import basics.thymeleaf.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
public class LoginController {

  private final LoginService loginService;
  private final SessionManager sessionManager;

  @GetMapping("/login")
  public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
    return "login/loginForm";
  }

//  @PostMapping("/login")
  public String login(@Valid @ModelAttribute LoginForm form,
                      BindingResult bindingResult,
                      HttpServletResponse response) {
    if (bindingResult.hasErrors()) {
      return "login/loginForm";
    }

    Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
    if (loginMember == null) {
      bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
      return "login/loginForm";

    }
    
    Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
    response.addCookie(idCookie);

    return "redirect:/";
  }

//  @PostMapping("/login")
  public String loginV2(@Valid @ModelAttribute LoginForm form,
                      BindingResult bindingResult,
                      HttpServletResponse response) {
    if (bindingResult.hasErrors()) {
      return "login/loginForm";
    }

    Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
    if (loginMember == null) {
      bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
      return "login/loginForm";

    }

    sessionManager.createSession(loginMember, response);

    Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
    response.addCookie(idCookie);

    return "redirect:/";
  }

//  @PostMapping("/login")
  public String loginV3(@Valid @ModelAttribute LoginForm form,
                        BindingResult bindingResult,
                        HttpServletRequest request) {
    if (bindingResult.hasErrors()) {
      return "login/loginForm";
    }

    Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
    if (loginMember == null) {
      bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
      return "login/loginForm";

    }

    HttpSession session = request.getSession();
    session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
    return "redirect:/";
  }

  @PostMapping("/login")
  public String loginV4(@Valid @ModelAttribute LoginForm form,
                        BindingResult bindingResult,
                        @RequestParam(defaultValue = "/") String redirectURL,
                        HttpServletRequest request) {
    if (bindingResult.hasErrors()) {
      return "login/loginForm";
    }

    Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
    if (loginMember == null) {
      bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
      return "login/loginForm";

    }

    HttpSession session = request.getSession();
    session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
    return "redirect:" + redirectURL;
  }

//  @PostMapping("/logout")
  public String logout(HttpServletResponse response) {
    expireCookie(response, "memberId");
    return "redirect:/";
  }

//  @PostMapping("/logout")
  public String logoutV2(HttpServletRequest request) {
    sessionManager.expire(request);
    return "redirect:/";
  }

  @PostMapping("/logout")
  public String logoutV3(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    return "redirect:/";
  }

  private void expireCookie(HttpServletResponse response, String cookieName) {
    Cookie cookie = new Cookie(cookieName, null);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}
