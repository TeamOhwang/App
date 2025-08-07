package com.example.backend.controller;

import com.example.backend.entity.Users;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 모든 사용자 조회
    @GetMapping
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    // 특정 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Long id) {
        Optional<Users> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }

    // 사용자 생성
    @PostMapping
    public ResponseEntity<Users> createUser(@RequestBody Users user) {
        try {
            Users savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 테스트용 사용자 생성
    @PostMapping("/test")
    public ResponseEntity<String> createTestUser() {
        try {
            Users testUser = Users.builder()
                    .email("test@example.com")
                    .nickname("테스트 사용자")
                    .profileImage("default.jpg")
                    .build();
            userRepository.save(testUser);
            return ResponseEntity.ok("테스트 사용자가 생성되었습니다!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("사용자 생성 실패: " + e.getMessage());
        }
    }

    // DB 연결 테스트
    @GetMapping("/test")
    public ResponseEntity<String> testDatabase() {
        try {
            long count = userRepository.count();
            return ResponseEntity.ok("DB 연결 성공! 현재 사용자 수: " + count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("DB 연결 실패: " + e.getMessage());
        }
    }

    // 소셜 로그인 (Google)
    @PostMapping("/social-login")
    public ResponseEntity<Map<String, Object>> socialLogin(@RequestBody Map<String, String> loginData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String accountCode = loginData.get("account_code");
            String email = loginData.get("email");
            String nickname = loginData.get("nickname");
            String profileImage = loginData.get("profileImage");

            // 기존 사용자 확인
            Optional<Users> existingUser = userRepository.findByAccount_code(accountCode);
            Users user;

            if (existingUser.isPresent()) {
                // 기존 사용자 정보 업데이트
                user = existingUser.get();
                user.updateNickname(nickname);
                if (profileImage != null && !profileImage.isEmpty()) {
                    user.updateProfileImage(profileImage);
                }
                userRepository.save(user);
            } else {
                // 새 사용자 생성
                user = Users.builder()
                        .account_code(accountCode)
                        .email(email)
                        .nickname(nickname)
                        .profileImage(profileImage)
                        .build();
                user = userRepository.save(user);
            }

            // 세션에 사용자 정보 저장
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userNickname", user.getNickname());

            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("userId", user.getId());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그인 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 현재 로그인된 사용자 정보 조회
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            
            if (userId == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(401).body(response);
            }

            Optional<Users> user = userRepository.findById(userId);
            if (user.isPresent()) {
                Users currentUser = user.get();
                response.put("success", true);
                response.put("userId", currentUser.getId());
                response.put("email", currentUser.getEmail());
                response.put("nickname", currentUser.getNickname());
                response.put("profileImage", currentUser.getProfileImage());
                response.put("accountCode", currentUser.getAccount_code());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "사용자 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 세션 유효성 확인
    @GetMapping("/check-session")
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId != null) {
            response.put("success", true);
            response.put("message", "세션이 유효합니다");
        } else {
            response.put("success", false);
            response.put("message", "세션이 만료되었습니다");
        }
        
        return ResponseEntity.ok(response);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            session.invalidate(); // 세션 무효화
            
            response.put("success", true);
            response.put("message", "로그아웃 성공");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "로그아웃 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}