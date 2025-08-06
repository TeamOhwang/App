package com.example.backend.domain.bookmark.DTO;

import lombok.Getter;

@Getter
public class PostWithLikeCountDto {
    private final Long postId;
    private final String content;
    private final long  likeCount;

    public PostWithLikeCountDto(Long postId, String content, long  likeCount) {
        this.postId = postId;
        this.content = content;
        this.likeCount = likeCount;
    }
}
