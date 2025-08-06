package com.example.backend.handler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket 연결 성공: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("받은 메시지: " + payload);

        // 모든 연결된 클라이언트에게 메시지 브로드캐스트
        for (WebSocketSession webSocketSession : sessions.values()) {
            if (webSocketSession.isOpen()) {
                webSocketSession.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("WebSocket 연결 종료: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("WebSocket 에러: " + session.getId() + ", " + exception.getMessage());
        sessions.remove(session.getId());
    }

    // 메시지 DTO 클래스
    public static class ChatMessage {
        private String sender;
        private String content;
        private String timestamp;

        public ChatMessage() {}

        public ChatMessage(String sender, String content, String timestamp) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}