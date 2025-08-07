package com.example.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 정적 리소스 핸들러를 설정하는 클래스입니다.
 * 웹에서 접근하는 '/images/**' 경로를 실제 파일 시스템의 경로에 매핑합니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // application.properties에서 file.upload-dir 속성 값을 주입받습니다.
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // '/images/'로 시작하는 모든 요청을 로컬 파일 시스템의 uploadDir 경로로 매핑합니다.
        // 이렇게 해야 클라이언트가 업로드된 이미지에 접근할 수 있습니다.
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
