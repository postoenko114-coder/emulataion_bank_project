package com.example.demo.controllers.admin;


import com.example.demo.dto.notification.NotificationDTOAdmin;
import com.example.demo.models.notification.Notification;
import com.example.demo.models.notification.TypeNotification;
import com.example.demo.models.user.User;
import com.example.demo.services.notification.NotificationService;
import com.example.demo.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/notifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;

    private final UserService userService;

    public AdminNotificationController(NotificationService notificationService,  UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public List<NotificationDTOAdmin> getUserNotifications(@PathVariable Long userId ,@RequestParam(required = false, defaultValue = "0") Integer page) {
        return getNotifications(notificationService.getAllUserNotification(userId, PageRequest.of(page,10)));
    }

    @GetMapping("/{notificationId}")
    public NotificationDTOAdmin getUserNotification(@PathVariable Long notificationId) {
        return notificationService.getNotificationForAdmin(notificationId);
    }

    @PostMapping("/sendNotification")
    public ResponseEntity<String> sendNotification(@PathVariable Long userId, @RequestParam String message, @RequestParam String typeNotification) {
        User user = userService.getUserById(userId);
        Notification notification = notificationService.createNotification(user, TypeNotification.valueOf(typeNotification.toUpperCase()), message);
        notificationService.sendNotificationToClient(user, notification);
        return ResponseEntity.ok().body("Notification send successfully");
    }

    private List<NotificationDTOAdmin> getNotifications(List<Notification> notifications) {
        List<NotificationDTOAdmin> list = new ArrayList<>();
        for (Notification notification : notifications) {
            list.add(notification.toDTOAdmin());
        }
        return list;
    }

}
