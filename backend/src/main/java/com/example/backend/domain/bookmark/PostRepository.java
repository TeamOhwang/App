package com.example.backend.domain.bookmark;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.domain.post.Post;

public interface PostRepository extends JpaRepository<Post, Long>   {
    List<Post> findByUser_Id(Long userId); 
}
