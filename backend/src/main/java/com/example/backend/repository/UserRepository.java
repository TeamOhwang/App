package com.example.backend.repository;

import com.example.backend.domain.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    
    // 이메일로 사용자 찾기
    Optional<Users> findByEmail(String email);
    
    // 이름으로 사용자 찾기
    Optional<Users> findByName(String nickname);
    
    // 이메일 존재 여부 확인
    boolean existsByEmail(String email);
}