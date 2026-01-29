package com.example.demo.dto.support;

import java.time.LocalDateTime;

public class SupportDTO {

    private Long id;

    private String subject;

    private String message;

    private String userEmail;

    private LocalDateTime createdAt;

    public SupportDTO() {}

    public SupportDTO(Long id, String subject, String message, String userEmail,  LocalDateTime createdAt) {
        this.subject = subject;
        this.message = message;
        this.userEmail = userEmail;
        this.createdAt = createdAt;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}

    public String getSubject() {return subject;}

    public void setSubject(String subject) {this.subject = subject;}

    public String getUserEmail() {return userEmail;}

    public void setUserEmail(String userEmail) {this.userEmail = userEmail;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}
