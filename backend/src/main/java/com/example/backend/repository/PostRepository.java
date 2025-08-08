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

    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC") // 최신순으로 정렬
    @Override
    List<Post> findAll();


}
