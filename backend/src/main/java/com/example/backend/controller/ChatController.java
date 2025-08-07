package com.example.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.chat.ChatMessage;
import com.example.backend.repository.ChatMessageRepository;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/history")
    public List<ChatHistoryResponse> getChatHistory() {
        // 데이터베이스에서 채팅 히스토리 조회 (시간순 정렬)
        List<ChatMessage> messages = chatMessageRepository.findAllByOrderByTimestampAsc();

        // DTO로 변환
        return messages.stream()
                .map(message -> new ChatHistoryResponse(
                        message.getSender(),
                        message.getContent(),
                        message.getTimestamp().toString(),
                        message.getMessageType(),
                        message.getImageUrl()))
                .collect(Collectors.toList());
    }

    // 채팅 히스토리 응답 DTO
    public static class ChatHistoryResponse {
        private String sender;
        private String content;
        private String timestamp;
        private String messageType = "text";
        private String imageUrl;

        public ChatHistoryResponse() {
        }

        public ChatHistoryResponse(String sender, String content, String timestamp, String messageType, String imageUrl) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.messageType = messageType;
            this.imageUrl = imageUrl;
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

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}