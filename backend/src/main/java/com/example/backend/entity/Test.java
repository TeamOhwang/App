package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test")
public class Test {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "message")
    private String message;
    
    // 기본 생성자
    public Test() {}
    
    // 생성자
    public Test(String message) {
        this.message = message;
    }
    
    // Getter, Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", message='" + message + '\'' +
                '}';
    }
}