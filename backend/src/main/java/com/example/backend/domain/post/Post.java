package com.example.backend.domain.post;

import java.util.ArrayList;
import java.util.List;

import com.example.backend.domain.comment.Comment;
import com.example.backend.domain.like.Like;
import com.example.backend.domain.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
 
    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Like> likes = new ArrayList<>();

    
    @Builder
    public Post(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
