package example.demoservice.web.product;

import example.demoservice.product.Product;
import example.demoservice.product.ProductRepository;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/products")
@Controller
public class ProductController {

  private final ProductRepository productRepository;

  @GetMapping
  public String products(Model model) {
    List<Product> products = productRepository.findAll();
    model.addAttribute("products", products);
    return "basic/products";
  }

  @GetMapping("/{productId}")
  public String product(@PathVariable long productId, Model model) {
    Product product = productRepository.findById(productId);
    model.addAttribute("product", product);
    return "basic/product";
  }

  @PostConstruct
  public void init() {
    productRepository.save(Product.builder()
        .name("productA")
        .price(10000)
        .quantity(20)
        .build());
    productRepository.save(Product.builder()
        .name("productB")
        .price(20000)
        .quantity(10)
        .build());
  }
}
