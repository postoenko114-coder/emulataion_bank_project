package com.example.demo.controllers.client;

import com.example.demo.dto.notification.NotificationDTO;
import com.example.demo.models.notification.Notification;
import com.example.demo.services.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationDTO> getAllNotifications(@PathVariable Long userId, @RequestParam(required = false, defaultValue = "0") Integer page) {
        return getNotificationDTOs(notificationService.getAllUserNotification(userId, PageRequest.of(page, 10)));
    }

    @GetMapping("/{notificationId}")
    public NotificationDTO getNotification(@PathVariable Long notificationId) {
        return notificationService.getNotificationByIdAndMarkAsRead(notificationId).toDTO();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteAllNotifications(@PathVariable Long userId) {
        notificationService.deleteAllNotificationInUser(userId);
        return ResponseEntity.ok("Notifications have been deleted");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok("Notification has been deleted");
    }

    private List<NotificationDTO> getNotificationDTOs(List<Notification> notifications) {
        List<NotificationDTO> list = new ArrayList<>();
        for (Notification notification : notifications) {
            list.add(notification.toDTO());
        }
        return list;
    }
}
