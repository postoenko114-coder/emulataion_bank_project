package com.example.demo.models.notification;

import com.example.demo.dto.notification.NotificationDTO;
import com.example.demo.dto.notification.NotificationDTOAdmin;
import com.example.demo.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeNotification typeNotification;

    private String message;

    @Enumerated(EnumType.STRING)
    private StatusNotification statusNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(TypeNotification typeNotification, String message, StatusNotification statusNotification) {
        this.typeNotification = typeNotification;
        this.message = message;
        this.statusNotification = statusNotification;
        this.createdAt = LocalDateTime.now();
    }

    public NotificationDTO toDTO() {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(id);
        notificationDTO.setTypeNotification(typeNotification);
        notificationDTO.setMessage(message);
        notificationDTO.setCreatedAt(createdAt);
        return notificationDTO;
    }

    public NotificationDTOAdmin toDTOAdmin() {
        NotificationDTOAdmin notificationDTOAdmin = new NotificationDTOAdmin();
        notificationDTOAdmin.setId(id);
        notificationDTOAdmin.setStatusNotification(statusNotification);
        notificationDTOAdmin.setTypeNotification(typeNotification);
        notificationDTOAdmin.setMessage(message);
        notificationDTOAdmin.setCreatedAt(createdAt);
        return notificationDTOAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatusNotification getStatus() {
        return statusNotification;
    }

    public void setStatus(StatusNotification statusNotification) {
        this.statusNotification = statusNotification;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TypeNotification getType() {
        return typeNotification;
    }

    public void setType(TypeNotification type) {
        this.typeNotification = type;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
