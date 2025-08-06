package com.example.backend.domain.bookmark;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.bookmark.DTO.PostWithLikeCountDto;
import com.example.backend.domain.bookmark.DTO.UserProfileDto;
import com.example.backend.domain.post.Post;
import com.example.backend.entity.Users;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class MypageController {
        private final MypageService mypageService;

// 내 프로필 불러오기
@GetMapping("/mypage")
public ResponseEntity<UserProfileDto> getMyProfile(HttpSession session) {
    Users user = (Users) session.getAttribute("loginUser");
    if (user == null) return ResponseEntity.status(401).build();

    Users foundUser = mypageService.getMyProfileEntity(user.getId()); 
    return ResponseEntity.ok(new UserProfileDto(foundUser));
}

// 닉네임 수정
@PatchMapping("/mypage/nickname")
public ResponseEntity<Void> updateNickname(@RequestParam String nickname, HttpSession session) {
    Users user = (Users) session.getAttribute("loginUser");
    if (user == null) return ResponseEntity.status(401).build();

    mypageService.updateMypage(user.getId(), nickname);
    return ResponseEntity.ok().build();
}

// 좋아요한 게시글 불러오기
@GetMapping("/mypage/likes")
public ResponseEntity<List<Post>> getLikedPosts(HttpSession session) {
    Users user = (Users) session.getAttribute("loginUser");
    if (user == null) return ResponseEntity.status(401).build();

    return ResponseEntity.ok(mypageService.getLikedPosts(user.getId()));
}

// 내 게시글과 좋아요 수 불러오기
@GetMapping("/mypage/posts-with-likes")
public ResponseEntity<List<PostWithLikeCountDto>> getMyPostsWithLikeCount(HttpSession session) {
    Users user = (Users) session.getAttribute("loginUser");
    if (user == null) return ResponseEntity.status(401).build();

    return ResponseEntity.ok(mypageService.myPostsWithLikeCount(user.getId()));
}

// 회원 탈퇴
@DeleteMapping("/mypage/withdraw")
public ResponseEntity<String> withdrawUser(HttpSession session) {
    Users user = (Users) session.getAttribute("loginUser");
    if (user == null) return ResponseEntity.status(401).build();

    mypageService.deleteUser(user.getId());
    session.invalidate(); // 로그아웃 처리
    return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
}


}

