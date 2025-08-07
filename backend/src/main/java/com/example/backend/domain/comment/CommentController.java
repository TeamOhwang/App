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
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;



// 댓글 또는 대댓글 작성
@PostMapping("/comments/{postId}")
public ResponseEntity<Void> createComment(
    @PathVariable Long postId,
    @RequestBody CommentRequestDto dto,
    HttpSession session
) {
    try {
        // 세션 유저 확인
        Users user = (Users) session.getAttribute("loginUser");
        if (user == null) {
            System.out.println("⛔ 세션에서 유저를 찾을 수 없습니다.");
            return ResponseEntity.status(401).build();
        }

        // 디버깅 로그 출력
        System.out.println(" 댓글 작성 요청 도착");
        System.out.println("postId: " + postId);
        System.out.println("content: " + dto.getContent());
        System.out.println(" parentId: " + dto.getParentId());
        System.out.println("세션 유저 ID: " + user.getId());
        System.out.println("세션 유저 닉네임: " + user.getNickname());

        // 서비스 호출
        commentService.createComment(postId, dto, user);
        System.out.println("댓글 저장 완료");

        return ResponseEntity.ok().build();

    } catch (Exception e) {
        System.out.println(" 예외 발생: " + e.getMessage());
        e.printStackTrace(); 
        return ResponseEntity.status(500).build();
    }
}


    
    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
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
    @GetMapping("/postAll/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }
}