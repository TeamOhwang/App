package com.example.backend.domain.bookmark;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.bookmark.DTO.PostThumbDto;
import com.example.backend.domain.bookmark.DTO.PostWithLikeCountDto;
import com.example.backend.domain.bookmark.DTO.UserProfileUpdateDto;
import com.example.backend.domain.like.LikeRepository;
import com.example.backend.entity.Users;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    // 내 프로필 불러오기
    @Transactional(readOnly = true)
    public Users getMyProfileEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 닉네임 , 프로필 이미지 수정
    @Transactional
    public void updateProfile(Long userId, UserProfileUpdateDto dto) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.updateNickname(dto.getNickname());
        }

        if (dto.getProfileImage() != null && !dto.getProfileImage().isBlank()) {
            user.updateProfileImage(dto.getProfileImage());
        }
    }



// 좋아요한 게시글 (썸네일 DTO)
    @Transactional(readOnly = true)
    public List<PostThumbDto> getLikedPosts(Long userId) {
        return likeRepository.findLikedPostsByUserId(userId).stream()
                .map(p -> new PostThumbDto(p.getId(), p.getImgUrl()))
                .toList();
    }

    // 내 게시글 + 좋아요 수 + 이미지
    @Transactional(readOnly = true)
    public List<PostWithLikeCountDto> myPostsWithLikeCount(Long userId) {
        return postRepository.findByUser_Id(userId).stream()
                .map(p -> new PostWithLikeCountDto(
                        p.getId(),
                        p.getImgUrl(),        // ★ DTO와 일치
                        p.getContent(),
                        (long) p.getLikes().size()))
                .toList();
    }

    // 회원탈퇴
    @Transactional
    public void deleteUser(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

}
