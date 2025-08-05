package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 이름으로 사용자 찾기
    Optional<User> findByName(String nickname);
    
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
}