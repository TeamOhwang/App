package com.example.backend.domain.bookmark;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final postRepository postRepository;



@Transactional // 회원 정보 조회
public UserProfileResponseDto getMyProfile(Long userId) {
     User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return new UserProfileResponseDto(
        user.getEmail(),
        user.getNickname(),
        user.getProfileImage(),
        user.getCreatedAt()
        );
}


@Transactional  //닉네임수정
public void updateMypage(Long userId, String nickname) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    user.updateNickname(nickname); 
}


@Transactional //내 게시글 불러오기
public List<Post> myPostsLoad(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return postRepository.findByUserId(userId);
}






    
}
