package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ImageUploadController {

    // 애플리케이션의 properties 파일에서 이미지 저장 경로를 읽어옵니다.
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 클라이언트로부터 이미지를 업로드받아 서버에 저장하고, 저장된 이미지의 URL을 반환합니다.
     * 엔드포인트: POST /api/upload/image
     *
     * @param file 클라이언트가 보낸 이미지 파일 (MultipartFile)
     * @return 저장된 이미지의 공개 URL이 포함된 JSON 응답
     */
    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "파일이 비어있습니다."));
        }

        try {
            // 업로드 디렉토리가 없으면 생성합니다.
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일 이름 생성 (중복 방지)
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // 파일 저장 경로 설정
            Path filePath = uploadPath.resolve(uniqueFileName);

            // 파일을 서버에 저장
            Files.copy(file.getInputStream(), filePath);

            // 클라이언트에 반환할 URL 생성
            // 서버의 IP와 포트를 실제 환경에 맞게 설정해야 합니다.
            // 여기서는 임시로 하드코딩하지만, 실제로는 동적으로 생성해야 합니다.
            String baseUrl = "http://192.168.219.180:8081"; // 실제 서버 주소로 변경
            String imageUrl = baseUrl + "/images/" + uniqueFileName;

            // 성공 응답 반환
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
