package com.example.backend.domain.bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.domain.user.Users;


@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    
}
