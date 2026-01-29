package com.example.demo.repositories;

import com.example.demo.models.notification.Notification;
import com.example.demo.models.notification.StatusNotification;
import com.example.demo.models.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUser(User user, Pageable pageable);

    Long countNotificationsByUserAndStatusNotification(User user, StatusNotification statusNotification);

    void deleteAllByUser(User user);
}
