package com.example.backend.domain.like;

import org.springframework.stereotype.Service;

import com.example.backend.domain.post.Post;
import com.example.backend.domain.post.PostRepository;
import com.example.backend.entity.Users;
import com.example.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 좋아요 누르기 (중복 체크만)
    @Transactional
    public void like(Long postId, Long userId) {
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) return;

        Users user = userRepository.getReferenceById(userId);
        Post post = postRepository.getReferenceById(postId);

        likeRepository.save(new Like(user, post));
    }

    // 좋아요 취소
    @Transactional
    public void unlike(Long postId, Long userId) {
        likeRepository.findByUserIdAndPostId(userId, postId)
            .ifPresent(likeRepository::delete);
    }

    
}
