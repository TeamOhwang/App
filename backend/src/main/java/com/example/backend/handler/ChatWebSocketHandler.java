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
        System.out.println("WebSocket 연결 성공: " + session.getId() + ", 총 연결 수: " + sessions.size());
        
        // 연결 성공 메시지 전송 (시스템 메시지는 DB에 저장하지 않음)
        ChatMessageDto welcomeMessage = new ChatMessageDto("System", "채팅방에 연결되었습니다.", 
                                                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
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
            ChatMessage chatMessage;
            if ("image".equals(messageDto.getMessageType())) {
                chatMessage = ChatMessage.createImageMessage(
                    messageDto.getSender(), 
                    messageDto.getContent(), 
                    messageDto.getImageUrl()
                );
            } else {
                chatMessage = ChatMessage.create(messageDto.getSender(), messageDto.getContent());
            }
            chatMessageRepository.save(chatMessage);
            
            // 저장된 메시지를 기반으로 응답 생성
            ChatMessageDto responseDto = new ChatMessageDto(
                chatMessage.getSender(), 
                chatMessage.getContent(), 
                chatMessage.getTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                chatMessage.getMessageType(),
                chatMessage.getImageUrl()
            );
            String responsePayload = objectMapper.writeValueAsString(responseDto);

            // 모든 연결된 클라이언트에게 메시지 브로드캐스트
            System.out.println("메시지 브로드캐스트 시작, 연결된 세션 수: " + sessions.size());
            int sentCount = 0;
            for (WebSocketSession webSocketSession : sessions.values()) {
                if (webSocketSession.isOpen()) {
                    try {
                        webSocketSession.sendMessage(new TextMessage(responsePayload));
                        sentCount++;
                    } catch (Exception e) {
                        System.err.println("메시지 전송 실패 (세션 ID: " + webSocketSession.getId() + "): " + e.getMessage());
                        // 연결이 끊어진 세션 제거
                        sessions.remove(webSocketSession.getId());
                    }
                } else {
                    // 닫힌 세션 제거
                    sessions.remove(webSocketSession.getId());
                }
            }
            System.out.println("메시지 전송 완료: " + sentCount + "개 세션에 전송");
        } catch (Exception e) {
            System.err.println("메시지 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("WebSocket 연결 종료: " + session.getId() + ", 남은 연결 수: " + sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 에러: " + session.getId() + ", " + exception.getMessage());
        exception.printStackTrace();
        sessions.remove(session.getId());
        System.out.println("에러로 인한 세션 제거 완료, 남은 연결 수: " + sessions.size());
    }

    // 메시지 DTO 클래스
    public static class ChatMessageDto {
        private String sender;
        private String content;
        private String timestamp;
        private String messageType = "text";
        private String imageUrl;

        public ChatMessageDto() {}

        public ChatMessageDto(String sender, String content, String timestamp) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.messageType = "text";
        }
        
        public ChatMessageDto(String sender, String content, String timestamp, String messageType, String imageUrl) {
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
            this.messageType = messageType;
            this.imageUrl = imageUrl;
        }

        // Getters and Setters
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
}