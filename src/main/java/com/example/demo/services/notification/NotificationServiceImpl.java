package com.example.demo.services.notification;

import com.example.demo.dto.notification.NotificationDTOAdmin;
import com.example.demo.models.account.Account;
import com.example.demo.models.notification.Notification;
import com.example.demo.models.notification.StatusNotification;
import com.example.demo.models.notification.TypeNotification;
import com.example.demo.models.user.User;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository,SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    @Override
    public Notification createNotification(User user, TypeNotification typeNotification, String message){
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(typeNotification);
        notification.setStatus(StatusNotification.NEW);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        return notification;
    }

    @Transactional
    @Override
    public Notification getNotificationByIdAndMarkAsRead(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setStatus(StatusNotification.READ);
        return notification;
    }

    @Transactional
    @Override
    public Notification updateNotification(Long notification_id, String typeNotification, String message){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if(!TypeNotification.valueOf(typeNotification.toUpperCase()).equals(notification.getType())){
            notification.setType(TypeNotification.valueOf(typeNotification.toUpperCase()));
        }
        if(!message.equals(notification.getMessage())){
            notification.setMessage(message);
        }
        return notification;
    }

    @Transactional
    @Override
    public void notifyDeposit(Account account, BigDecimal amount){
        String message = "Your account: " + account.getAccountNumber() + " has been deposited by " + amount + " " + account.getCurrencyAccount() + "\nNew balance: " + String.format("%.2f", account.getBalance());
        sendNotificationToClient(account.getUser(), createNotification(account.getUser(), TypeNotification.DEPOSIT, message));
    }

    @Transactional
    @Override
    public void notifyWithdrawal(Account account, BigDecimal amount){
        String message = "From your account: " + account.getAccountNumber() + " has been withdrawn by " + amount + " " + account.getCurrencyAccount() + "\nNew balance: " + String.format("%.2f", account.getBalance());
        sendNotificationToClient(account.getUser(), createNotification(account.getUser(), TypeNotification.WITHDRAWAL, message));
    }

    @Transactional
    @Override
    public void notifyTransfer(Account accountFrom, Account accountTo, BigDecimal amount){
        String messageTo = "To your account " + accountTo.getAccountNumber() + " was sent transfer from account " + accountFrom.getAccountNumber() + " for the amount " + amount + " "
                + accountTo.getCurrencyAccount() + System.lineSeparator() + "\nNew balance: " + String.format("%.2f", accountTo.getBalance());
        String messageFrom = "From your account " + accountFrom.getAccountNumber() + " was sent transfer to account " + accountTo.getAccountNumber() + " for the amount " + amount + " "
                + accountFrom.getCurrencyAccount() + System.lineSeparator() + "\nNew balance: " + String.format("%.2f", accountFrom.getBalance());
        sendNotificationToClient(accountTo.getUser(), createNotification(accountTo.getUser(), TypeNotification.TRANSFER, messageTo));
        sendNotificationToClient(accountFrom.getUser(), createNotification(accountFrom.getUser(), TypeNotification.TRANSFER, messageFrom));
    }

    @Transactional
    @Override
    public void notifyPaymentByCard(Account account, BigDecimal amount){
        String message = "Payment by card for the amount " + amount + " " + account.getCurrencyAccount() + "\nNew balance: " + account.getBalance() ;
        sendNotificationToClient(account.getUser(), createNotification(account.getUser(), TypeNotification.CARD, message));
    }

    @Transactional
    @Override
    public void notifyPersonalMessage(Long user_id, String message){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        sendNotificationToClient(user, createNotification(user, TypeNotification.PERSONAL, message));
    }

    @Transactional
    @Override
    public void notifyAdvertisingMessage(Long user_id, String message){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        sendNotificationToClient(user, createNotification(user, TypeNotification.ADVERTISING, message));
    }

    @Transactional
    @Override
    public void sendNotificationToClient(User user, Notification notification) {
        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notification);
        markAsSent(notification.getId());
    }

    @Transactional
    @Override
    public List<Notification> getNewNotificationsOfUser(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        List<Notification> notifications = notificationRepository.findAllByUser(user, pageable);
        List<Notification> newNotifications = new ArrayList<>();
        for(Notification notification : notifications){
            if(notification.getStatus().equals(StatusNotification.SENT)){
                newNotifications.add(notification);
            }
        }
        return newNotifications;
    }
    @Transactional
    @Override
    public NotificationDTOAdmin getNotificationForAdmin(Long notification_id){
        return notificationRepository.findById(notification_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found")).toDTOAdmin();
    }

    @Transactional
    @Override
    public List<Notification> getAllUserNotification(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return notificationRepository.findAllByUser(user, pageable);
    }

    @Transactional
    @Override
    public Long getUnreadCount(Long user_id) {
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        return notificationRepository.countNotificationsByUserAndStatusNotification(user, StatusNotification.SENT);
    }

    @Transactional
    @Override
    public void deleteNotification(Long notification_id){
        notificationRepository.deleteById(notification_id);
    }

    @Transactional
    @Override
    public void deleteAllNotificationInUser(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));
        notificationRepository.deleteAllByUser(user);
    }

    public void markAsSent(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setStatus(StatusNotification.SENT);
    }

    public void markAsRead(Long notification_id){
        Notification notification = notificationRepository.findById(notification_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setStatus(StatusNotification.READ);
    }

}
