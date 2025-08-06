package com.example.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.entity.Users;
import com.example.backend.domain.post.Post;
import com.example.backend.domain.post.PostService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // 게시글 생성
    @PostMapping("/post")
    public String createPost(@RequestBody Post post, HttpSession session) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) {
            return "로그인이 필요합니다.";
        }
        postService.createPost(post, user); 
        return "게시글 등록 완료";
    }


    // 게시글 전체 조회
    @GetMapping("/read")
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }
    
    // 게시글 단건 조회
    @GetMapping("/post")
    public ResponseEntity<Post> getPostById(@RequestParam Long postId) {
          Post posts  = postService.getPostById(postId);
          return ResponseEntity.ok(posts);
    }

    // 게시글 수정
     @PutMapping("/{postId}")
    public String updatePost(@PathVariable Long postId, @RequestBody Post updatedPost,HttpSession session) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) return "로그인이 필요합니다.";
        postService.updatePost(postId, updatedPost, user);
        return "게시글 수정 완료";
    }


    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public String deletePost(@PathVariable Long postId, HttpSession session) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) return "로그인이 필요합니다.";
        postService.deletePost(postId, user);
        return "게시글 삭제 완료";
    }

}
    
