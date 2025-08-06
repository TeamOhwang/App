package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.post.Post;
import com.example.backend.domain.post.PostService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시글 생성
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody Post post
                                        // HttpSession으로 사용자 정보 
                                        , HttpSession session
                                        ) {
        Long userId = (Long) session.getAttribute("userId");
        // Long userId = 1L; 테스트용
        postService.create(post, userId);
        return ResponseEntity.ok("게시글이 등록되었습니다");
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        return ResponseEntity.ok(post);
    }


    // 전체 게시글 목록 조회
    // @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    // 특정 유저 게시글 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable Long userId) {
        List<Post> posts = postService.getPostByUserId(userId);
        return ResponseEntity.ok(posts);
    }

    // 본인 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody Post requestPost, HttpSession session) {
        // 로그인한 사용자 ID
        Long userId = (Long) session.getAttribute("userId");

        postService.updatePost(postId, requestPost, userId);

        return ResponseEntity.ok("게시글이 수정되었습니다");
    }

    // 본인 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }

        postService.deletePost(postId, userId);

        return ResponseEntity.ok("게시글이 삭제되었습니다");
    }
}
