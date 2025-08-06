package com.example.backend.domain.like;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.post.Post;


@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    @Query("SELECT l.post FROM Like l WHERE l.user.id = :userId")
    List<Post> findLikedPostsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);
    
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    int countByPostId(@Param("postId") Long postId);
    
}
