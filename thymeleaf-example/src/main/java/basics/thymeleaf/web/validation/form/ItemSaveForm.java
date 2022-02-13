package basics.thymeleaf.web.validation.form;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class ItemSaveForm {

  @NotBlank
  private String itemName;

  @NotNull
  @Range(min = 1000, max = 1000000)
  private Integer price;

  @Max(value = 9999)
  @NotNull
  private Integer quantity;
}
