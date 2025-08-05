package com.example.backend.domain.bookmark.DTO;

import lombok.Getter;

@Getter
public class PostWithLikeCountDto {
    private final Long postId;
    private final String title;
    private final String description;
    private final long  likeCount;

    public PostWithLikeCountDto(Long postId, String title, String description, long  likeCount) {
        this.postId = postId;
        this.title = title;
        this.description = description;
        this.likeCount = likeCount;
    }
}
