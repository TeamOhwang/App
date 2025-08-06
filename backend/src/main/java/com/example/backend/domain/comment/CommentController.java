package com.example.backend.domain.comment;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.comment.DTO.CommentRequestDto;
import com.example.backend.entity.Users;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글/대댓글 작성
    @PostMapping("/{postId}")
    public ResponseEntity<Void> createComment(
        @PathVariable Long postId,
        @RequestBody CommentRequestDto dto,
        HttpSession session
    ) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).build();

        commentService.createComment(postId, dto, user);
        return ResponseEntity.ok().build();
    }

    
    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
        @PathVariable Long commentId,
        HttpSession session
    ) {
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).build();

        commentService.deleteComment(commentId, user);
        return ResponseEntity.ok().build();
    }


    // 게시글 댓글 + 대댓글 전체 조회
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }
}