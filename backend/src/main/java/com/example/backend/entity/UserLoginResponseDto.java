package com.example.backend.entity;

import lombok.Getter;

@Getter
public class UserLoginResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
    private String accountCode;

    public UserLoginResponseDto(Users user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.accountCode = user.getAccount_code();
    }
}
