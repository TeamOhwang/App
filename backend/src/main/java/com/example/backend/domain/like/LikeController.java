package com.example.backend.domain.like;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
        private final LikeService likeService;

    //좋아요누르기
    @PostMapping("/{postId}")
    public ResponseEntity<Void> like(@PathVariable Long postId, @RequestParam Long userId) {
        likeService.like(postId, userId);
        return ResponseEntity.ok().build();
    }
    
    //좋아요취소
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unlike(@PathVariable Long postId, @RequestParam Long userId) {
        likeService.unlike(postId, userId);
        return ResponseEntity.ok().build();
    }

    
}
