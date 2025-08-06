package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.backend.domain.post.Post;
import com.example.backend.entity.Users;

public interface PostRepository extends JpaRepository<Post, Long>{
    
    // 특정 인물의 게시물 가져오기
    List<Post> findByUser (Users user);
    List<Post> findByUser_Id(Long userId);

    @Query("SELECT p FROM Post p JOIN FETCH p.user") // Post 엔티티를 p 라는 별칭으로 사용. Post를 조회하는데 연결된 user 엔티티를 즉시 가지고 와라
    @Override
    List<Post> findAll();


}
