package com.example.demoproject.web.dto;

import com.example.demoproject.domain.posts.Posts;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostsSaveRequest {
    private final String title;
    private final String content;
    private final String author;

    @Builder
    @JsonCreator
    PostsSaveRequest(@JsonProperty("title") String title,
                     @JsonProperty("content") String content,
                     @JsonProperty("author") String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public Posts toEntity() {
        return Posts.builder()
            .title(title)
            .content(content)
            .author(author)
            .build();
    }
}
