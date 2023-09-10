package com.example.demoproject.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.demoproject.config.auth.LoginUser;
import com.example.demoproject.config.auth.dto.SessionUser;
import com.example.demoproject.service.PostsService;
import com.example.demoproject.web.dto.PostsListResponse;
import com.example.demoproject.web.dto.PostsResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final HttpSession httpSession;

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        List<PostsListResponse> postsListResponseList = postsService.findAllDesc().stream()
            .map(PostsListResponse::new)
            .collect(Collectors.toList());

        model.addAttribute("posts", postsListResponseList);

        if (user != null) {
            model.addAttribute("userName", user.getName());
        }

        return "index";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model) {
        model.addAttribute("post", new PostsResponse(postsService.findById(id)));
        return "posts-update";
    }

}
