package example.demoservice.product;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ProductRepositoryTest {

  ProductRepository productRepository = new ProductRepository();

  @AfterEach
  void after() {
    productRepository.clear();
  }

  @Test
  void save() {
    // given
    Product product = Product.builder()
        .name("상품1")
        .price(1000)
        .quantity(10)
        .build();
    // when
    productRepository.save(product);
    // then
    Product findProduct = productRepository.findById(product.getId());
    assertThat(findProduct.getPrice()).isEqualTo(product.getPrice());
  }

  @Test
  void findById() {
    // given
    Product productA = Product.builder()
        .name("상품1")
        .price(1000)
        .quantity(10)
        .build();
    Product productB = Product.builder()
        .name("상품2")
        .price(10000)
        .quantity(20)
        .build();
    // when
    productRepository.save(productA);
    productRepository.save(productB);
    // then
    Product findProduct = productRepository.findById(productA.getId());
    assertThat(findProduct.getName()).isEqualTo(productA.getName());
  }

  @Test
  void findAll() {
    // given
    Product productA = Product.builder()
        .name("상품1")
        .price(1000)
        .quantity(10)
        .build();
    Product productB = Product.builder()
        .name("상품2")
        .price(10000)
        .quantity(20)
        .build();
    // when
    productRepository.save(productA);
    productRepository.save(productB);
    // then
    List<Product> products = productRepository.findAll();
    assertThat(products.size()).isEqualTo(2);
    assertThat(products).contains(productA, productB);
  }

  @Test
  void update() {
    // given
    Product productA = Product.builder()
        .name("상품1")
        .price(1000)
        .quantity(10)
        .build();
    // when
    Product savedProduct = productRepository.save(productA);
    Long id = savedProduct.getId();
    UpdateProductDto updateProductDto = UpdateProductDto.builder()
        .name("업데이트 상품1")
        .price(2000)
        .quantity(5)
        .build();

    productRepository.update(id, updateProductDto);

    // then
    Product findProduct = productRepository.findById(id);

    assertThat(findProduct.getName()).isEqualTo(updateProductDto.getName());
    assertThat(findProduct.getPrice()).isEqualTo(updateProductDto.getPrice());
    assertThat(findProduct.getQuantity()).isEqualTo(updateProductDto.getQuantity());
  }
}
