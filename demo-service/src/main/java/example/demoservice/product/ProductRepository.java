package example.demoservice.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

  private static final Map<Long, Product> store = new ConcurrentHashMap<>();
  private static AtomicLong sequence = new AtomicLong();

  public Product save(Product product) {
    product.addId(sequence.incrementAndGet());
    store.put(product.getId(), product);
    return product;
  }
  
  public Product findById(Long id) {
    return store.get(id);
  }

  public List<Product> findAll() {
    return new ArrayList<>(store.values());
  }

  public void update(Long id, UpdateProductDto updateParam) {
    Product product = findById(id);
    product.update(updateParam);
  }

  public void clear() {
    store.clear();
  }
}
