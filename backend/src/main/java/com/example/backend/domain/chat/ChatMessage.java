package com.example.backend.domain.chat;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@Getter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String messageType = "text"; // "text" 또는 "image"
    private String imageUrl;

    @Builder
    public ChatMessage(String sender, String content, LocalDateTime timestamp, String messageType, String imageUrl) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.messageType = messageType != null ? messageType : "text";
        this.imageUrl = imageUrl;
    }

    // 정적 팩토리 메서드
    public static ChatMessage create(String sender, String content) {
        return ChatMessage.builder()
                .sender(sender)
                .content(content)
                .timestamp(LocalDateTime.now())
                .messageType("text")
                .build();
    }
    
    // 이미지 메시지용 정적 팩토리 메서드
    public static ChatMessage createImageMessage(String sender, String content, String imageUrl) {
        return ChatMessage.builder()
                .sender(sender)
                .content(content)
                .timestamp(LocalDateTime.now())
                .messageType("image")
                .imageUrl(imageUrl)
                .build();
    }
}
