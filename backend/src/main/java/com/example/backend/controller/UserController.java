package com.example.backend.controller;

import com.example.backend.domain.user.Users;
import com.example.backend.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowCredentials = "true") // 세션 쿠키 허용
public class UserController {

	@Autowired
	private UserRepository userRepository;

	// 모든 사용자 조회
	@GetMapping
	public List<Users> getAllUsers() {
		return userRepository.findAll();
	}

	// 특정 사용자 조회
	@GetMapping("/{id}")
	public ResponseEntity<Users> getUserById(@PathVariable Long id) {
		Optional<Users> user = userRepository.findById(id);
		if (user.isPresent()) {
			return ResponseEntity.ok(user.get());
		}
		return ResponseEntity.notFound().build();
	}

	// 사용자 생성
	@PostMapping
	public ResponseEntity<Users> createUser(@RequestBody Users user) {
		try {
			Users savedUser = userRepository.save(user);
			return ResponseEntity.ok(savedUser);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	// 소셜 로그인 (세션 생성)
	@PostMapping("/social-login")
	public ResponseEntity<Map<String, Object>> socialLogin(@RequestBody Users incomingUser, HttpSession session) {

		Map<String, Object> response = new HashMap<>();

		return userRepository.findByEmail(incomingUser.getEmail()).map(user -> {
			// 기존 사용자 로그인
			// 세션에 사용자 정보 저장
			session.setAttribute("userId", user.getId());
			session.setAttribute("userEmail", user.getEmail());
			session.setAttribute("userNickname", user.getNickname());
			session.setAttribute("userProfileImage", user.getProfileImage());
			session.setAttribute("userAccountCode", user.getAccount_code());
			session.setAttribute("isLoggedIn", true);

			response.put("success", true);
			response.put("message", "로그인 성공");
			response.put("user", user);
			response.put("sessionId", session.getId()); // 디버깅용

			return ResponseEntity.ok(response);
		}).orElseGet(() -> {
			// 신규 사용자 회원가입
			Users newUser = Users.builder().account_code(incomingUser.getAccount_code()).email(incomingUser.getEmail())
					.nickname(incomingUser.getNickname()).profileImage(incomingUser.getProfileImage()).build();

			Users savedUser = userRepository.save(newUser);

			// 세션에 신규 사용자 정보 저장
			session.setAttribute("userId", savedUser.getId());
			session.setAttribute("userEmail", savedUser.getEmail());
			session.setAttribute("userNickname", savedUser.getNickname());
			session.setAttribute("userProfileImage", savedUser.getProfileImage());
			session.setAttribute("userAccountCode", savedUser.getAccount_code());
			session.setAttribute("isLoggedIn", true);

			response.put("success", true);
			response.put("message", "회원가입 성공");
			response.put("user", savedUser);
			response.put("sessionId", session.getId());

			return ResponseEntity.ok(response);
		});
	}
}
