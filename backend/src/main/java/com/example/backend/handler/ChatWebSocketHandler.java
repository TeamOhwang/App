package com.example.backend.handler;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.backend.domain.chat.ChatMessage;
import com.example.backend.repository.ChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket 연결 성공: " + session.getId());
        
        // 연결 성공 메시지 전송
        ChatMessageDto welcomeMessage = new ChatMessageDto("System", "채팅방에 연결되었습니다.", 
                                                    java.time.LocalDateTime.now().toString());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("받은 메시지: " + payload);

        try {
            // JSON 메시지를 파싱
            ChatMessageDto messageDto = objectMapper.readValue(payload, ChatMessageDto.class);
            
            // DB에 메시지 저장
            ChatMessage chatMessage = ChatMessage.create(messageDto.getSender(), messageDto.getContent());
            chatMessageRepository.save(chatMessage);
            
            // 저장된 메시지를 기반으로 응답 생성
            ChatMessageDto responseDto = new ChatMessageDto(
                chatMessage.getSender(), 
                chatMessage.getContent(), 
                chatMessage.getTimestamp().toString()
            );
            String responsePayload = objectMapper.writeValueAsString(responseDto);

            // 모든 연결된 클라이언트에게 메시지 브로드캐스트
            for (WebSocketSession webSocketSession : sessions.values()) {
                if (webSocketSession.isOpen()) {
                    webSocketSession.sendMessage(new TextMessage(responsePayload));
                }
            }
        } catch (Exception e) {
            System.err.println("메시지 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
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
    public static class ChatMessageDto {
        private String sender;
        private String content;
        private String timestamp;

        public ChatMessageDto() {}

        public ChatMessageDto(String sender, String content, String timestamp) {
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