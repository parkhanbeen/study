package exception.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @ResponseStatus 어노테이션은 커스텀 exception 에만 사용할 수 있다.
 * 특정 조건에 따라 동적으로 변경하기도 어렵다..
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "error.bad")
public class BadRequestException extends RuntimeException {

}
