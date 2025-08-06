package com.example.backend.domain.like;

import org.springframework.stereotype.Service;

import com.example.backend.domain.post.Post;
import com.example.backend.entity.Users;
import com.example.backend.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;


       // 좋아요 누르기
    @Transactional
    public void like(Long postId, Users user) {
        if (likeRepository.existsByUserIdAndPostId(user.getId(), postId)) return;

        Post post = postRepository.getReferenceById(postId);
        likeRepository.save(new Like(user, post));
    }

    // 좋아요 취소
    @Transactional
    public void unlike(Long postId, Users user) {
        likeRepository.findByUserIdAndPostId(user.getId(), postId)
            .ifPresent(likeRepository::delete);
    }

    
}
