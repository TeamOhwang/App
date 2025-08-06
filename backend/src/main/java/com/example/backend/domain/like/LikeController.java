package com.example.backend.domain.like;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.entity.Users;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
        private final LikeService likeService;

       // 좋아요 누르기
    @PostMapping("/{postId}")
    public ResponseEntity<Void> like(@PathVariable Long postId, HttpSession session) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(401).build(); // 로그인 안 했을 때 401 반환
        }

        likeService.like(postId, user);
        return ResponseEntity.ok().build();
    }

    // 좋아요 취소
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unlike(@PathVariable Long postId, HttpSession session) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        likeService.unlike(postId, user);
        return ResponseEntity.ok().build();
    }

    
}
