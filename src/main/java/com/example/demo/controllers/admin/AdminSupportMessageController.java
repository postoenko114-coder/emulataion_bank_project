package com.example.demo.controllers.admin;

import com.example.demo.dto.support.SupportDTO;
import com.example.demo.dto.support.SupportReplyDTO;
import com.example.demo.models.notification.TypeNotification;
import com.example.demo.models.user.User;
import com.example.demo.services.EmailService;
import com.example.demo.services.notification.NotificationService;
import com.example.demo.services.supportMessage.SupportMessageService;
import com.example.demo.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/supportMessages")
public class AdminSupportMessageController {
    @Autowired
    private SupportMessageService supportMessageService;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmailService emailService;

    public AdminSupportMessageController(SupportMessageService supportMessageService, UserService userService, NotificationService notificationService, EmailService emailService) {
        this.supportMessageService = supportMessageService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @GetMapping
    public List<SupportDTO> getAllSupportMessages() {
        return supportMessageService.getAllSupportMessages();
    }

    @GetMapping("/{supportMessageId}")
    public SupportDTO getSupportMessage(@PathVariable("supportMessageId") Long supportMessageId) {
        return supportMessageService.getSupportMessageById(supportMessageId);
    }

    @GetMapping("/search")
    public List<SupportDTO> search(@RequestParam(required = false) String email,
                                   @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date) {
        return supportMessageService.search(email, date);
    }

    @PostMapping("/{supportMessageId}/reply")
    public ResponseEntity<String> replyToMessage(@PathVariable Long supportMessageId, @RequestBody SupportReplyDTO replyDTO) {
        SupportDTO originalMsg = supportMessageService.getSupportMessageById(supportMessageId);

        String targetEmail = originalMsg.getUserEmail();
        if (targetEmail == null || targetEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("No email found in the support message");
        }

        User user = userService.findUserByEmail(targetEmail);

        String replyText = "RE: Support Request (ID: " + supportMessageId + ")\n\n" + replyDTO.getReplyText();

        if (user != null) {
            notificationService.sendNotificationToClient(user, notificationService.createNotification(user, TypeNotification.SUPPORT, replyText));
            return ResponseEntity.ok("User found. Reply sent to Dashboard Notifications.");
        } else {
            try {
                emailService.sendSimpleEmail(targetEmail, "Support Reply - MyBank", replyText);
                return ResponseEntity.ok("User not found in DB. Reply sent via Email.");
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("Error sending email: " + e.getMessage());
            }
        }
    }

}
