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

    @Builder
    public ChatMessage(String sender, String content, LocalDateTime timestamp) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    // 정적 팩토리 메서드
    public static ChatMessage create(String sender, String content) {
        return ChatMessage.builder()
                .sender(sender)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
