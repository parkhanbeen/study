package com.example.demoproject.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.demoproject.service.PostsService;
import com.example.demoproject.web.dto.PostsResponse;
import com.example.demoproject.web.dto.PostsSaveRequest;
import com.example.demoproject.web.dto.PostsUpdateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class PostsApiController {

    private final PostsService postsService;

    @PostMapping("/api/v1/posts")
    public Long save(@RequestBody PostsSaveRequest request) {
        return postsService.save(request);
    }

    @PutMapping("/api/v1/posts/{id}")
    public Long update(@PathVariable Long id, @RequestBody PostsUpdateRequest request) {
        return postsService.update(id, request);
    }

    @GetMapping("/api/v1/posts/{id}")
    public PostsResponse findById(@PathVariable Long id) {
        return new PostsResponse(postsService.findById(id));
    }

    @DeleteMapping("/api/v1/posts/{id}")
    public Long delete(@PathVariable Long id) {
        postsService.delete(id);
        return id;
    }

}
