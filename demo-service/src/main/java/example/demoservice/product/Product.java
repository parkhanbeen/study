package example.demoservice.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

  private Long id;
  private String name;
  private Integer price;
  private Integer quantity;

  @Builder
  public Product(String name, Integer price, Integer quantity) {
    this.name = name;
    this.price = price;
    this.quantity = quantity;
  }

  public void addId(long id) {
    this.id = id;
  }

  public void update(UpdateProductDto updateParam) {
    this.name = updateParam.getName();
    this.price = updateParam.getPrice();
    this.quantity = updateParam.getQuantity();
  }
}
