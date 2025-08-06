package com.example.backend.domain.post;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.entity.Users;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 생성
    @Transactional
    public void create(Post req, Long userId) {

        // 로그인한 사용자인지 확인
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("회원만 게시글을 작성할 수 있습니다"));

        // 게시글 생성
        Post post = new Post();
        post.setContent(req.getContent());
        post.setUser(user); // 작성자 설정

        // 저장
        postRepository.save(post);

    }

    // 게시글 상세 조회
    public Post getPostById(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));
    }

    // 게시글 전체 조회
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    // 특정 사용자가 쓴 게시글 보기, 조회 대상이 누구든 상관 없음
    public List<Post> getPostByUserId(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다"));

        return postRepository.findByUser(user);
    }

    // 본인 게시글만 수정
    public void updatePost(Long postId, Post requestPost, Long currentMemberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));

        if (!post.getUser().getId().equals(currentMemberId)) {
            throw new RuntimeException("자신의 게시글만 수정할 수 있습니다");
        }

        post.setContent(requestPost.getContent());

        postRepository.save(post);
    }

    // 본인 게시글 삭제
    public void deletePost(Long postId, Long currentMemberId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));

        if (!post.getUser().getId().equals(currentMemberId)) {
            throw new RuntimeException("자신의 게시글만 삭제할 수 있습니다");
        }

        postRepository.delete(post);
    }
}
