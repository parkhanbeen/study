package basics.thymeleaf.web.validation;

import basics.thymeleaf.domain.item.Item;
import basics.thymeleaf.domain.item.ItemRepository;
import basics.thymeleaf.domain.item.SaveCheck;
import basics.thymeleaf.domain.item.UpdateCheck;
import basics.thymeleaf.web.validation.form.ItemSaveForm;
import basics.thymeleaf.web.validation.form.ItemUpdateForm;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

//    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price",
                "range",
                new Object[]{1000, 1000000},
                null);
        }

        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v4/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItemV2(@Validated @ModelAttribute("item") ItemSaveForm form,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {

        if (form.getPrice() == null || form.getPrice() < 1000 || form.getPrice() > 1000000) {
            bindingResult.rejectValue("price",
                "range",
                new Object[]{1000, 1000000},
                null);
        }

        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v4/addForm";
        }

        Item item = new Item(form.getItemName(), form.getPrice(), form.getQuantity());

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    private boolean hasError(boolean empty) {
        return !empty;
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

//    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId,
                       @Validated @ModelAttribute Item item,
                       BindingResult bindingResult) {

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price",
                "range",
                new Object[]{1000, 1000000},
                null);
        }

        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v4/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @PostMapping("/{itemId}/edit")
    public String editV2(@PathVariable Long itemId,
                         @Validated @ModelAttribute ItemUpdateForm form,
                         BindingResult bindingResult) {

        if (form.getPrice() == null || form.getPrice() < 1000 || form.getPrice() > 1000000) {
            bindingResult.rejectValue("price",
                "range",
                new Object[]{1000, 1000000},
                null);
        }

        if (bindingResult.hasErrors()) {
            log.info("errors = {}", bindingResult);
            return "validation/v4/editForm";
        }

        Item item = new Item(form.getItemName(), form.getPrice(), form.getQuantity());
        itemRepository.update(itemId, item);
        return "redirect:/validation/v4/items/{itemId}";
    }

}

