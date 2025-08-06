package com.example.backend.domain.comment.DTO;

import lombok.Getter;

@Getter
public class CommentRequestDto {
    private String content;
    private Long parentId; // 대댓글이면 부모 댓글 ID, 일반 댓글이면 null
}
