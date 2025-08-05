package com.example.backend.domain.like;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.post.Post;


@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
        @Query("SELECT l.post FROM Like l WHERE l.user.id = :userId")
    List<Post> findLikedPostsByUserId(@Param("userId") Long userId);
    
}
