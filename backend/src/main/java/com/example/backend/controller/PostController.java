package com.example.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import com.example.backend.entity.Users;
import com.example.backend.domain.post.Post;
import com.example.backend.domain.post.PostService;
import com.example.backend.domain.post.DTO.PostResponseDto;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {
    private final PostService postService;

    // 게시글 생성
    @PostMapping("/post")
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody Post post, HttpSession session) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(errorResponse);
        }
        
        try {
            postService.createPost(post, user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "게시글 등록 완료");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "게시글 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    // 게시글 전체 조회
    @GetMapping("/read")
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts()); // DTO 반환
    }
    
    // 게시글 단건 조회
    @GetMapping("/post/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
          Post posts  = postService.getPostById(postId);
          return ResponseEntity.ok(posts);
    }

    // 게시글 수정
     @PutMapping("/post/{postId}")
    public String updatePost(@PathVariable Long postId, @RequestBody Post updatedPost,HttpSession session) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) return "로그인이 필요합니다.";
        postService.updatePost(postId, updatedPost, user);
        return "게시글 수정 완료";
    }


    // 게시글 삭제
    @DeleteMapping("/post/{postId}")
    public String deletePost(@PathVariable Long postId, HttpSession session) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) return "로그인이 필요합니다.";
        postService.deletePost(postId, user);
        return "게시글 삭제 완료";
    }

}
    
