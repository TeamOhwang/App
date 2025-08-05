package com.example.backend.controller;

import com.example.backend.entity.Test;
import com.example.backend.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MessageController {
    
    @Autowired
    private TestRepository testRepository;
    
    @GetMapping("/message")
    public MessageResponse getMessage() {
        // test 테이블에서 첫 번째 메시지 가져오기
        List<Test> tests = testRepository.findAll();
        if (!tests.isEmpty()) {
            String message = tests.get(0).getMessage();
            return new MessageResponse(message);
        }
        return new MessageResponse("DB에 메시지가 없습니다. test 테이블을 확인해주세요.");
    }
    
    public static class MessageResponse {
        private String message;
        
        public MessageResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}