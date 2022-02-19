package exception.example.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ApiExceptionController {

  @GetMapping("/api/members/{id}")
  public MemberDto getMember(@PathVariable("id") String id) {
    if ("exception".equals(id)) {
      throw new RuntimeException("잘못된 사용자 입니다.");
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
