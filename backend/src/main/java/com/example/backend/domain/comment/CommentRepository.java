package com.example.backend.domain.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.domain.post.Post;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Post> findByUserId(Long userId); 
    
    
} 