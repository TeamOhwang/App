package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    /**
     * 이메일로 사용자를 찾는 메서드입니다.
     * 
     * @param email 찾을 사용자의 이메일
     * @return 이메일에 해당하는 Users 엔티티를 포함하는 Optional 객체
     */
    Optional<Users> findByEmail(String email);

    // 아이디로 사용자를 찾는 메서드
    Optional<Users> findById(Long userId);

    /**
     * 닉네임으로 사용자를 찾는 메서드입니다.
     * Users 엔티티에 'nickname' 필드가 있으므로, findByNickname으로 수정했습니다.
     * 
     * @param nickname 찾을 사용자의 닉네임
     * @return 닉네임에 해당하는 Users 엔티티를 포함하는 Optional 객체
     */
    Optional<Users> findByNickname(String nickname);

    /**
     * 이메일 존재 여부를 확인하는 메서드입니다.
     * 
     * @param email 존재 여부를 확인할 이메일
     * @return 이메일이 존재하면 true, 아니면 false
     */
    boolean existsByEmail(String email);

    /**
     * account_code로 사용자를 찾는 메서드입니다.
     * 
     * @param accountCode 찾을 사용자의 account_code
     * @return account_code에 해당하는 Users 엔티티를 포함하는 Optional 객체
     */
    @Query("SELECT u FROM Users u WHERE u.account_code = :accountCode")
    Optional<Users> findByAccount_code(@Param("accountCode") String accountCode);
}
