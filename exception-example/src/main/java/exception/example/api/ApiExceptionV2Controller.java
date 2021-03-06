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
