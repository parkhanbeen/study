package example.demoservice.web.product;

import example.demoservice.product.Product;
import example.demoservice.product.ProductRepository;
import example.demoservice.product.UpdateProductDto;
import example.demoservice.web.product.request.CreateProductCommand;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

  @GetMapping("/add")
  public String addForm() {
    return "basic/addForm";
  }

//  @PostMapping("/add")
  public String saveV1(@RequestParam String productName,
                       @RequestParam int price,
                       @RequestParam Integer quantity,
                       Model model) {

    Product savedProduct = productRepository.save(
        Product.builder()
            .name(productName)
            .price(price)
            .quantity(quantity)
            .build()
    );
    model.addAttribute("product", savedProduct);

    return "basic/product";

  }

//    @PostMapping("/add")
    public String saveV2(CreateProductCommand command, Model model) {
      Product savedProduct = productRepository.save(
          Product.builder()
              .name(command.getName())
              .price(command.getPrice())
              .quantity(command.getQuantity())
              .build()
      );

    model.addAttribute("product", savedProduct);

    return "basic/product";
  }

//  @PostMapping("/add")
  public String saveV3(@ModelAttribute Product product) {
    productRepository.save(product);
    return "basic/product";
  }

  @PostMapping("/add")
  public String saveV4(Product product) {
    productRepository.save(product);
    return "basic/product";
  }

  @GetMapping("/{productId}/edit")
  public String editForm(@PathVariable Long productId, Model model) {
    Product findProduct = productRepository.findById(productId);
    model.addAttribute("product", findProduct);

    return "basic/editForm";
  }

  @PostMapping("/{productId}/edit")
  public String edit(@PathVariable Long productId, UpdateProductDto updateProductDto) {
    productRepository.update(productId, updateProductDto);
    return "redirect:/products/{productId}";
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
