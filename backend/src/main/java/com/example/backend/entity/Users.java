package com.example.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.backend.domain.chat.ChatMessage;
import com.example.backend.domain.comment.Comment;
import com.example.backend.domain.like.Like;
import com.example.backend.domain.post.Post;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String account_code;
    private String email;
    private String nickname;
    private String profileImage;  // 프로필 이미지 URL
    
    
    // 연관 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Like> likes = new ArrayList<>();
    
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();

    @Builder
    public Users(String account_code, String email, String nickname, String profileImage) {
        this.account_code = account_code;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    // 사용자 정보 수정 메소드
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 프로필 이미지 수정 메소드
    public void updateProfileImage(String imageUrl) {
        this.profileImage = imageUrl;
    }
}