package basics.thymeleaf.domain.item;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;

@Data
//@ScriptAssert(lang = "javascript",
//    script = "_this.price * _this.quantity >= 10000",
//    message = "총합이 10000 넘게 입력해주세요.")
public class Item {

    private Long id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;

    @Max(9999)
    @NotNull
    private Integer quantity;

    /**
     * 판매 여부
     */
    private Boolean open;
    /**
     * 등록 지역
     */
    private List<String> regions;
    /**
     * 상품 종류
     */
    private ItemType itemType;
    /**
     * 배송 방식
     */
    private String deliveryCode;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
