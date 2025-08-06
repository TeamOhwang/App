package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.backend.domain.chat.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 최신 메시지부터 조회 (최대 100개)
    @Query("SELECT c FROM ChatMessage c ORDER BY c.timestamp DESC")
    List<ChatMessage> findTop100ByOrderByTimestampDesc();

    // 시간순으로 모든 메시지 조회
    List<ChatMessage> findAllByOrderByTimestampAsc();
}