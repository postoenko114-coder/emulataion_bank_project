package com.example.demo.dto.notification;

import com.example.demo.models.notification.TypeNotification;
import java.time.LocalDateTime;

public class NotificationDTO {

    private Long id;

    private TypeNotification typeNotification;

    private String message;

    private LocalDateTime createdAt;

    public NotificationDTO() {}

    public NotificationDTO(Long id, TypeNotification typeNotification, String message, LocalDateTime createdAt) {
        this.id = id;
        this.typeNotification = typeNotification;
        this.message = message;
        this.createdAt = createdAt;
    }
    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public String getMessage() {return message;}

    public void setMessage(String message) {this.message = message;}

    public TypeNotification getTypeNotification() {return typeNotification;}

    public void setTypeNotification(TypeNotification typeNotification) {this.typeNotification = typeNotification;}
}
