package basics.thymeleaf.basic;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/basic")
public class BasicController {

    @GetMapping("/text-basic")
    public String textBasic(Model model) {
        model.addAttribute("data", "<b>hello</b>");
        return "basic/text-basic";
    }

    @GetMapping("/text-unescaped")
    public String textUnescape(Model model) {
        model.addAttribute("data", "<b>hello</b>");
        return "basic/text-unescaped";
    }

    @GetMapping("/variable")
    public String variable(Model model) {
        User userA = new User("park", 30);
        User userB = new User("kim", 10);

        List<User> list = List.of(userA, userB);

        Map<String, User> userMap = new HashMap<>();
        userMap.put("userA", userA);
        userMap.put("userB", userB);

        model.addAttribute("user", userA);
        model.addAttribute("users", list);
        model.addAttribute("userMap", userMap);

        return "basic/variable";
    }

    @Getter
    static class User {
        private final String username;
        private final int age;

        public User(String username, int age) {
            this.username = username;
            this.age = age;
        }
    }
}
