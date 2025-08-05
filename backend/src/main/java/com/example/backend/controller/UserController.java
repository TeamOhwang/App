// package com.example.backend.controller;

// import com.example.backend.entity.User;
// import com.example.backend.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Optional;

// @RestController
// @RequestMapping("/api/users")
// @CrossOrigin(origins = "*")
// public class UserController {
    
//     @Autowired
//     private UserRepository userRepository;
    
//     // 모든 사용자 조회
//     @GetMapping
//     public List<User> getAllUsers() {
//         return userRepository.findAll();
//     }
    
//     // 특정 사용자 조회
//     @GetMapping("/{id}")
//     public ResponseEntity<User> getUserById(@PathVariable Long id) {
//         Optional<User> user = userRepository.findById(id);
//         if (user.isPresent()) {
//             return ResponseEntity.ok(user.get());
//         }
//         return ResponseEntity.notFound().build();
//     }
    
//     // 사용자 생성
//     @PostMapping
//     public ResponseEntity<User> createUser(@RequestBody User user) {
//         try {
//             User savedUser = userRepository.save(user);
//             return ResponseEntity.ok(savedUser);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().build();
//         }
//     }
    
//     // 테스트용 사용자 생성
//     @PostMapping("/test")
//     public ResponseEntity<String> createTestUser() {
//         try {
//             User testUser = new User("테스트 사용자", "test@example.com");
//             userRepository.save(testUser);
//             return ResponseEntity.ok("테스트 사용자가 생성되었습니다!");
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body("사용자 생성 실패: " + e.getMessage());
//         }
//     }
    
//     // DB 연결 테스트
//     @GetMapping("/test")
//     public ResponseEntity<String> testDatabase() {
//         try {
//             long count = userRepository.count();
//             return ResponseEntity.ok("DB 연결 성공! 현재 사용자 수: " + count);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body("DB 연결 실패: " + e.getMessage());
//         }
//     }
// }