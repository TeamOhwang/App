package com.example.backend.domain.bookmark;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.bookmark.DTO.PostWithLikeCountDto;
import com.example.backend.domain.like.LikeRepository;
import com.example.backend.domain.post.Post;
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

//닉네임수정
@Transactional(readOnly = true)  
public void updateMypage(Long userId, String nickname) {
    Users user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    user.updateNickname(nickname); 
}

// 좋아요한 게시글 불러오기
@Transactional (readOnly = true)
public List<Post> getLikedPosts(Long userId) {
    return likeRepository.findLikedPostsByUserId(userId);
}

// 내 게시글과 좋아요 수 불러오기
@Transactional (readOnly = true)
public List<PostWithLikeCountDto> myPostsWithLikeCount(Long userId) {
    List<Post> posts = postRepository.findByUser_Id(userId);

    return posts.stream()
        .map(post -> new PostWithLikeCountDto(
            post.getId(),
            post.getContent(),
            (long) post.getLikes().size()
        ))
        .toList();
}

//회원탈퇴
@Transactional 
public void deleteUser(Long userId) {
    Users user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found")); 
    userRepository.delete(user);
}


}           


    

