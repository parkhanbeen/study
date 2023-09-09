package com.example.demoproject.web.dto;

import java.time.LocalDateTime;

import com.example.demoproject.domain.posts.Posts;
import lombok.Getter;

@Getter
public class PostsListResponse {

    private final Long id;
    private final String title;
    private final String author;
    private final LocalDateTime modifiedDate;

    public PostsListResponse(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.modifiedDate = entity.getModifiedDateTime();
    }
}
