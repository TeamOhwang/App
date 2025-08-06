package com.example.backend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @GetMapping("/history")
    public List<ChatHistoryResponse> getChatHistory() {
        // 임시 데이터 - 실제로는 데이터베이스에서 가져와야 함
        List<ChatHistoryResponse> history = new ArrayList<>();

        return history;
    }

    // 채팅 히스토리 응답 DTO
    public static class ChatHistoryResponse {
        private String sender;
        private String content;
        private String timestamp;

        public ChatHistoryResponse() {
        }

        public ChatHistoryResponse(String sender, String content, String timestamp) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }
}