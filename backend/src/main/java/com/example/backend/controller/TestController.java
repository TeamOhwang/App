// package com.example.backend.controller;

// import com.example.backend.entity.Test;
// import com.example.backend.repository.TestRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/test")
// @CrossOrigin(origins = "*")
// public class TestController {
    
//     @Autowired
//     private TestRepository testRepository;
    
//     // 모든 테스트 메시지 조회
//     @GetMapping("/messages")
//     public List<Test> getAllMessages() {
//         return testRepository.findAll();
//     }
    
//     // 첫 번째 메시지만 가져오기 (기존 API와 호환)
//     @GetMapping("/message")
//     public ResponseEntity<MessageResponse> getFirstMessage() {
//         List<Test> tests = testRepository.findAll();
//         if (!tests.isEmpty()) {
//             String message = tests.get(0).getMessage();
//             return ResponseEntity.ok(new MessageResponse(message));
//         }
//         return ResponseEntity.ok(new MessageResponse("테이블에 메시지가 없습니다."));
//     }
    
//     // 새 메시지 추가
//     @PostMapping("/message")
//     public ResponseEntity<Test> addMessage(@RequestBody Test test) {
//         try {
//             Test savedTest = testRepository.save(test);
//             return ResponseEntity.ok(savedTest);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().build();
//         }
//     }
    
//     // JSON 응답용 클래스
//     public static class MessageResponse {
//         private String message;
        
//         public MessageResponse(String message) {
//             this.message = message;
//         }
        
//         public String getMessage() {
//             return message;
//         }
        
//         public void setMessage(String message) {
//             this.message = message;
//         }
//     }
// }