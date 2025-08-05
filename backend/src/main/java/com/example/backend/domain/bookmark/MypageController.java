package com.example.backend.domain.bookmark;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.bookmark.DTO.UserProfileDto;
import com.example.backend.domain.post.Post;
import com.example.backend.domain.user.Users;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {
        private final MypageService mypageService;

@GetMapping("/profile/{userId}")//내 프로필 불러오기
public ResponseEntity<UserProfileDto> getMyProfile(@PathVariable Long userId) {
    Users user = mypageService.getMyProfileEntity(userId);
    return ResponseEntity.ok(new UserProfileDto(user));
}

@PatchMapping("/mypage/nickname")//닉네임 수정
public ResponseEntity<Void> updateNickname( @RequestParam String nickname, @RequestParam Long userId) {
    mypageService.updateMypage(userId, nickname);
    return ResponseEntity.ok().build();
}

@GetMapping("/mypage/posts/{userId}") // 내 게시글 불러오기 
public ResponseEntity<List<Post>> getMyPosts(@PathVariable Long userId) {
    return ResponseEntity.ok(mypageService.myPostsLoad(userId));
}



@GetMapping("/mypage/likes/{userId}") // 좋아요한 게시글 불러오기
public ResponseEntity<List<Post>> getLikedPosts(@PathVariable Long userId) {
    return ResponseEntity.ok(mypageService.getLikedPosts(userId));
}

}

