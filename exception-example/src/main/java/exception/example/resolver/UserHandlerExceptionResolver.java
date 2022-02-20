package exception.example.resolver;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.example.exception.UserException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class UserHandlerExceptionResolver implements HandlerExceptionResolver {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                       Object handler, Exception ex) {
    try {
      if (ex instanceof UserException) {
        log.info("UserExceptionResolver exception to 400");
        String acceptHeader = request.getHeader("accept");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        if (APPLICATION_JSON_VALUE.equals(acceptHeader)) {
          Map<String, Object> errorResult = new HashMap<>();
          errorResult.put("ex", ex.getClass());
          errorResult.put("message", ex.getMessage());

          String result = objectMapper.writeValueAsString(errorResult);

          response.setContentType(APPLICATION_JSON_VALUE);
          response.setCharacterEncoding("utf-8");
          response.getWriter().write(result);
          return new ModelAndView();
        } else {
          return new ModelAndView("error/500");
        }
      }

    } catch (IOException e) {
      log.error("resolver ex", e);
    }

    return null;
  }
}
