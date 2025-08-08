package com.example.backend.domain.post.DTO;


import java.time.LocalDateTime;

public record PostResponseDto(
        Long postId,
        String content,
        String imgUrl,
        LocalDateTime createdAt,
        Long userId,
        String nickname
) {}