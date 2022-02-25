package exception.example.controller;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FormatterController {

  @GetMapping("/formatter/edit")
  public String formatterForm(Model model) {
    model.addAttribute("form", new Form(10000, LocalDateTime.now()));
    return "formatter-form";
  }

  @PostMapping("/formatter/edit")
  public String formatterEdit(@ModelAttribute Form form) {
    return "formatter-view";
  }

  @Getter
  static class Form {
    @NumberFormat(pattern = "###,###")
    private Integer number;
    @DateTimeFormat(pattern = "yyyy-mm-dd HH:mm:ss")
    private LocalDateTime localDateTime;

    public Form(Integer number, LocalDateTime localDateTime) {
      this.number = number;
      this.localDateTime = localDateTime;
    }
  }

}
