package exception.example.exceptionhandler;

import exception.example.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "exception.example.api")
public class ExControllerAdvice {

  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  @ExceptionHandler(IllegalArgumentException.class)
  public ErrorResult illegalArgumentExceptionHandler(IllegalArgumentException e) {
    log.error("illegalArgumentExceptionHandler ex", e);
    return new ErrorResult("BAD", e.getMessage());
  }

  //  @ExceptionHandler(UserException.class)
  @ExceptionHandler  // 인자로 받을 경우 UserException.class 생략 가능
  public ResponseEntity<ErrorResult> userExceptionHandler(UserException e) {
    log.error("userExceptionHandler ex", e);
    return new ResponseEntity(new ErrorResult("USER-EXCEPTION", e.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler
  public ErrorResult exceptionHandler(Exception e) {
    log.error("Exception ex", e);
    return new ErrorResult("EX","시스템 장애!!");
  }

}
