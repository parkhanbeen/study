package exception.example.api;

import exception.example.exception.BadRequestException;
import exception.example.exception.UserException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
public class ApiExceptionController {

  @GetMapping("/api/members/{id}")
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

  @GetMapping("/api/response-status-ex1")
  public String responseStatusEx1() {
    throw new BadRequestException();
  }

  @GetMapping("/api/response-status-ex2")
  public String responseStatusEx2() {
    throw new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "error bad",
        new IllegalArgumentException());
  }

  @GetMapping("/api/default-handler-exception")
  public String defaultException(@RequestParam Integer data) {
    return "ok";
  }

  @Getter
  @RequiredArgsConstructor
  static class MemberDto {
    private final String memberId;
    private final String name;
  }

}
