package com.example.demo.dto.notification;

import com.example.demo.models.notification.StatusNotification;
import com.example.demo.models.notification.TypeNotification;

import java.time.LocalDateTime;

public class NotificationDTOAdmin extends NotificationDTO {

    private StatusNotification statusNotification;

    public NotificationDTOAdmin() {}

    public NotificationDTOAdmin(Long id, TypeNotification typeNotification, String message, LocalDateTime createdAt, StatusNotification statusNotification) {
        super(id, typeNotification, message, createdAt);
        this.statusNotification = statusNotification;
    }

    public StatusNotification getStatusNotification() {return statusNotification;}

    public void setStatusNotification(StatusNotification statusNotification) {this.statusNotification = statusNotification;}
}
