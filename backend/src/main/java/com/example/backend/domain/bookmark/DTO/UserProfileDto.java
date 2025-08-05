package com.example.backend.domain.bookmark.DTO;

import com.example.backend.domain.user.Users;

import lombok.Getter;

@Getter
public class UserProfileDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;


    public UserProfileDto(Users user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
    }
    
}

