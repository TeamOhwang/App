package com.example.backend.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String nickname;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    // 기본 생성자
    public User() {}
    
    // 생성자
    public User(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
    
    // Getter, Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return nickname;
    }
    
    public void setName(String nickname) {
        this.nickname = nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

        //닉네임 수정 메소드
    public void updateNickname(String nickname) {
        this.nickname = nickname;

    }

    
}