package exception.example.servlet;

import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static javax.servlet.RequestDispatcher.ERROR_EXCEPTION_TYPE;
import static javax.servlet.RequestDispatcher.ERROR_MESSAGE;
import static javax.servlet.RequestDispatcher.ERROR_REQUEST_URI;
import static javax.servlet.RequestDispatcher.ERROR_SERVLET_NAME;
import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class ErrorPageController {

  @RequestMapping("/error-page/404")
  public String errorPage404(HttpServletRequest request, HttpServletResponse response) {
    log.info("에러페이지 404");
    printErrorInfo(request);
    return "error-page/404";
  }

  @RequestMapping("/error-page/500")
  public String errorPage500(HttpServletRequest request, HttpServletResponse response) {
    log.info("에러페이지 500");
    printErrorInfo(request);
    return "error-page/500";
  }

  private void printErrorInfo(HttpServletRequest request) {
    log.info("ERROR_EXCEPTION: ex=",
        request.getAttribute(ERROR_EXCEPTION));
    log.info("ERROR_EXCEPTION_TYPE: {}",
        request.getAttribute(ERROR_EXCEPTION_TYPE));
    log.info("ERROR_MESSAGE: {}", request.getAttribute(ERROR_MESSAGE)); // ex의 경우 NestedServletException 스프링이 한번 감싸서 반환
    log.info("ERROR_REQUEST_URI: {}",
        request.getAttribute(ERROR_REQUEST_URI));
    log.info("ERROR_SERVLET_NAME: {}",
        request.getAttribute(ERROR_SERVLET_NAME));
    log.info("ERROR_STATUS_CODE: {}",
        request.getAttribute(ERROR_STATUS_CODE));
    log.info("dispatchType={}", request.getDispatcherType());
  }
}
