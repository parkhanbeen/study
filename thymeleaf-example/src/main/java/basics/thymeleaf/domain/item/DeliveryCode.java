package basics.thymeleaf.domain.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeliveryCode {
  private final String code;
  private final String displayName;
}
