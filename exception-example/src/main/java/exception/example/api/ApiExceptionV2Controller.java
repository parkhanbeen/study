package exception.example.api;

import exception.example.exception.UserException;
import exception.example.exceptionhandler.ErrorResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ApiExceptionV2Controller {

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

  @GetMapping("/api/v2/members/{id}")
  public MemberDto getMember(@PathVariable("id") String id) {
    if ("exception".equals(id)) {
      throw new RuntimeException("잘못된 사용자 입니다.");
    }

    if ("bad".equals(id)) {
      throw new IllegalArgumentException("잘못된 입력입니다.");
    }

    if ("user-ex".equals(id)) {
      throw new UserException("사용자 오류 입니다.");
    }
    return new MemberDto(id, "park han been" + id);
  }


  @Getter
  @RequiredArgsConstructor
  static class MemberDto {
    private final String memberId;
    private final String name;
  }
}
