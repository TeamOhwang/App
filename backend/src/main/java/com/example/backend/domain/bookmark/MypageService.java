package com.example.backend.domain.bookmark;

import java.util.List;
import org.springframework.stereotype.Service;

import com.example.backend.domain.like.LikeRepository;
import com.example.backend.domain.post.Post;
import com.example.backend.domain.user.Users;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;



@Transactional //내 프로필 불러오기
public Users getMyProfileEntity(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
}


@Transactional  //닉네임수정
public void updateMypage(Long userId, String nickname) {
    Users user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    user.updateNickname(nickname); 
}


@Transactional //내 게시글 불러오기
public List<Post> myPostsLoad(Long userId) {
    Users user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    return postRepository.findByUser_Id(userId); 
}



@Transactional //좋아요한 게시글 불러오기
public List<Post> getLikedPosts(Long userId) {
    return likeRepository.findLikedPostsByUserId(userId);
}










}           




    

