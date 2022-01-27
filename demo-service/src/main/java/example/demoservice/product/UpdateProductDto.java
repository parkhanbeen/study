package example.demoservice.product;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateProductDto {

  private final String name;
  private final Integer price;
  private final Integer quantity;

  @Builder
  public UpdateProductDto(String name, Integer price, Integer quantity) {
    this.name = name;
    this.price = price;
    this.quantity = quantity;
  }
}
