package com.example.demoproject.web.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostsUpdateRequest {
    private final String title;
    private final String content;

    @Builder
    @JsonCreator
    PostsUpdateRequest(@JsonProperty("title") String title,
                       @JsonProperty("content") String content) {
        this.title = title;
        this.content = content;
    }
}
