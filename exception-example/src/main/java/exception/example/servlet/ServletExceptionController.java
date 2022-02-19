package exception.example.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class ServletExceptionController {

  @GetMapping("/error-exception")
  public void errorException() {
    throw new RuntimeException("예외 발생!");
  }

  @GetMapping("/error-404")
  public void error404(HttpServletResponse response) throws IOException {
    response.sendError(404, "404 에러 발생!");
  }

  @GetMapping("/error-400")
  public void error400(HttpServletResponse response) throws IOException {
    response.sendError(400, "400 에러 발생!");
  }

  @GetMapping("/error-500")
  public void error500(HttpServletResponse response) throws IOException {
    response.sendError(500);
  }

}
