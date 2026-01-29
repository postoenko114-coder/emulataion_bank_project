package com.example.demo.services.notification;

import com.example.demo.dto.notification.NotificationDTOAdmin;
import com.example.demo.models.account.Account;
import com.example.demo.models.notification.Notification;
import com.example.demo.models.notification.TypeNotification;
import com.example.demo.models.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface NotificationService {

    @Transactional
    Notification createNotification(User user, TypeNotification typeNotification, String message);

    @Transactional
    Notification getNotificationByIdAndMarkAsRead(Long notification_id);

    @Transactional
    Notification updateNotification(Long notification_id, String typeNotification, String message);

    @Transactional
    void notifyDeposit(Account account, BigDecimal amount);

    @Transactional
    void notifyWithdrawal(Account account, BigDecimal amount);

    @Transactional
    void notifyTransfer(Account accountTo, Account accountFrom, BigDecimal amount);

    @Transactional
    void notifyPaymentByCard(Account account, BigDecimal amount);

    @Transactional
    void notifyPersonalMessage(Long user_id, String message);

    @Transactional
    void notifyAdvertisingMessage(Long user_id, String message);

    @Transactional
    void sendNotificationToClient(User user, Notification notification);

    @Transactional
    List<Notification> getNewNotificationsOfUser(Long user_id, Pageable pageable);

    @Transactional
    NotificationDTOAdmin getNotificationForAdmin(Long notification_id);

    @Transactional
    List<Notification> getAllUserNotification(Long user_id, Pageable pageable);

    @Transactional
    Long getUnreadCount(Long userId);

    @Transactional
    void deleteNotification(Long notification_id);

    @Transactional
    void deleteAllNotificationInUser(Long user_id);
}
