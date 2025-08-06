package com.example.backend.controller;

import com.example.backend.entity.Users;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // 세션 쿠키 허용
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 소셜 로그인 (세션 생성)
    @PostMapping("/social-login")
    public ResponseEntity<Map<String, Object>> socialLogin(
            @RequestBody Users incomingUser, 
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        return userRepository.findByEmail(incomingUser.getEmail()).map(user -> {
            // 기존 사용자 로그인
            saveUserToSession(session, user);
            
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            // 신규 사용자 회원가입
            Users newUser = Users.builder()
                    .account_code(incomingUser.getAccount_code())
                    .email(incomingUser.getEmail())
                    .nickname(incomingUser.getNickname())
                    .profileImage(incomingUser.getProfileImage())
                    .build();
                    
            Users savedUser = userRepository.save(newUser);
            saveUserToSession(session, savedUser);
            
            response.put("success", true);
            response.put("message", "회원가입 성공");
            response.put("user", savedUser);
            
            return ResponseEntity.ok(response);
        });
    }
    
    // 현재 로그인된 사용자 정보 조회
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        
        if (isLoggedIn != null && isLoggedIn) {
            Long userId = (Long) session.getAttribute("userId");
            String email = (String) session.getAttribute("userEmail");
            String nickname = (String) session.getAttribute("userNickname");
            String profileImage = (String) session.getAttribute("userProfileImage");
            String accountCode = (String) session.getAttribute("userAccountCode");
            
            response.put("success", true);
            response.put("userId", userId);
            response.put("email", email);
            response.put("nickname", nickname);
            response.put("profileImage", profileImage);
            response.put("accountCode", accountCode);
            response.put("isLoggedIn", true);
            
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "로그인되지 않은 상태입니다.");
            response.put("isLoggedIn", false);
            
            return ResponseEntity.status(401).body(response);
        }
    }
    
    // 로그아웃 (세션 무효화)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        session.invalidate(); // 세션 완전 삭제
        
        response.put("success", true);
        response.put("message", "로그아웃 성공");
        
        return ResponseEntity.ok(response);
    }
    
    // 세션 검증 (로그인 상태 확인)
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        
        if (isLoggedIn != null && isLoggedIn) {
            response.put("success", true);
            response.put("message", "유효한 세션");
            response.put("isLoggedIn", true);
        } else {
            response.put("success", false);
            response.put("message", "유효하지 않은 세션");
            response.put("isLoggedIn", false);
        }
        
        return ResponseEntity.ok(response);
    }
    
    // 헬퍼 메서드 - 세션에 사용자 정보 저장
    private void saveUserToSession(HttpSession session, Users user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userNickname", user.getNickname());
        session.setAttribute("userProfileImage", user.getProfileImage());
        session.setAttribute("userAccountCode", user.getAccount_code());
        session.setAttribute("isLoggedIn", true);
    }
}