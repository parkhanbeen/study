package example.demoservice.web.product.request;

import lombok.Getter;

@Getter
public class CreateProductCommand {
  private final String name;
  private final Integer price;
  private final Integer quantity;

  public CreateProductCommand(String name,
                              Integer price,
                              Integer quantity) {
    this.name = name;
    this.price = price;
    this.quantity = quantity;
  }
}
