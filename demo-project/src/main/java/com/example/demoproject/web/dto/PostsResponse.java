package com.example.demoproject.web.dto;

import com.example.demoproject.domain.posts.Posts;
import lombok.Getter;

@Getter
public class PostsResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String author;

    public PostsResponse(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.author = entity.getAuthor();
    }
}
