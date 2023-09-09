package com.example.demoproject.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demoproject.domain.posts.Posts;
import com.example.demoproject.domain.posts.PostsRepository;
import com.example.demoproject.web.dto.PostsResponse;
import com.example.demoproject.web.dto.PostsSaveRequest;
import com.example.demoproject.web.dto.PostsUpdateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PostsService {

    private final PostsRepository postsRepository;

    @Transactional
    public Long save(PostsSaveRequest request) {
        return postsRepository.save(request.toEntity()).getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequest request) {
        Posts findPosts = postsRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. 식별자 : " + id));
        findPosts.update(request.getTitle(), request.getContent());

        return id;
    }

    @Transactional(readOnly = true)
    public Posts findById(Long id) {
        return postsRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. 식별자 : " + id));
    }

    @Transactional(readOnly = true)
    public List<Posts> findAllDesc() {
        return postsRepository.findALlDesc();
    }

    @Transactional
    public void delete(Long id) {
        Posts findPosts = postsRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. 식별자 : " + id));

        postsRepository.delete(findPosts);
    }
}
