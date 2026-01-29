package com.example.demo.models.supportMessage;

import com.example.demo.dto.support.SupportDTO;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "supportMessages")
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String Subject;

    private String Message;

    private String userEmail;

    private StatusSupportMessage statusSupportMessage;

    private LocalDateTime createdAt;

    public SupportMessage() {}

    public SupportMessage(String Subject, String Message, String userEmail, StatusSupportMessage statusSupportMessage) {
        this.Subject = Subject;
        this.Message = Message;
        this.userEmail = userEmail;
        this.statusSupportMessage = statusSupportMessage;
        this.createdAt = LocalDateTime.now();
    }

    public SupportDTO toDTO() {
        SupportDTO supportDTO = new SupportDTO();
        supportDTO.setId(id);
        supportDTO.setSubject(Subject);
        supportDTO.setMessage(Message);
        supportDTO.setUserEmail(userEmail);
        supportDTO.setCreatedAt(createdAt);
        return supportDTO;
    }

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getMessage() {return Message;}

    public void setMessage(String message) {Message = message;}

    public StatusSupportMessage getStatusSupportMessage() {return statusSupportMessage;}

    public void setStatusSupportMessage(StatusSupportMessage statusSupportMessage) {this.statusSupportMessage = statusSupportMessage;}

    public String getSubject() {return Subject;}

    public void setSubject(String subject) {Subject = subject;}

    public String getUserEmail() {return userEmail;}

    public void setUserEmail(String userEmail) {this.userEmail = userEmail;}
}
