package com.example.backend.domain.post.DTO;

import java.time.LocalDateTime;

import com.example.backend.domain.post.Post;
import com.example.backend.domain.like.LikeRepository;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String content;
    private String imgUrl;
    private LocalDateTime createdAt;
    private UserInfo user;
    private int likeCount;
    private boolean isLiked;

    @Getter
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String nickname;
        private String profileImage;

        public UserInfo(Long id, String nickname, String profileImage) {
            this.id = id;
            this.nickname = nickname;
            this.profileImage = profileImage;
        }
    }

    public PostResponseDto(Post post, LikeRepository likeRepository, Long currentUserId) {
        this.id = post.getId();
        this.content = post.getContent();
        this.imgUrl = post.getImgUrl();
        this.createdAt = post.getCreatedAt();

        if (post.getUser() != null) {
            this.user = new UserInfo(
                    post.getUser().getId(),
                    post.getUser().getNickname(),
                    post.getUser().getProfileImage());
        }
        
        // 좋아요 정보 설정
        this.likeCount = likeRepository.countByPostId(post.getId());
        this.isLiked = currentUserId != null && likeRepository.existsByUserIdAndPostId(currentUserId, post.getId());
    }
}