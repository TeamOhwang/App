package com.example.backend.domain.post;


import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.domain.post.DTO.PostResponseDto;
import com.example.backend.entity.Users;
import com.example.backend.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;



    //게시글 생성
    @Transactional
    public void createPost(Post post, Users user) {
        post.setUser(user);
        postRepository.save(post);
    }

    //게시글 전체조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPosts() {
       List<Post> posts = postRepository.findAll(); // JOIN FETCH로 user까지 조회됨
    return posts.stream()
            .map(post -> new PostResponseDto(
                    post.getId(),
                    post.getContent(),
                    post.getImgUrl(),
                    post.getCreatedAt(),
                    post.getUser().getNickname()
            ))
            .toList();
}


    // 게시글 단건조회
    @Transactional(readOnly = true)
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));
    }

    // 게시글 수정
    @Transactional
    public void updatePost(Long postId, Post updatedPost, Users user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 수정할 수 있습니다.");
        }
        post.setContent(updatedPost.getContent());
        post.setImgUrl(updatedPost.getImgUrl());
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Users user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("작성자만 삭제할 수 있습니다.");
        }
        postRepository.delete(post);
    }

}

    