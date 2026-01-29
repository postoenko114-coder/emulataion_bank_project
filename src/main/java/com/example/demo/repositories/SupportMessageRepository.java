package com.example.demo.repositories;

import com.example.demo.models.supportMessage.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    Optional<SupportMessage> findSupportMessageById(Long id);

    @Query("SELECT m FROM SupportMessage m WHERE " +
            "(:email IS NULL OR m.userEmail LIKE %:email%) AND " +
            "(:date IS NULL OR CAST(m.createdAt AS LocalDate) = :date) " +
            "ORDER BY m.createdAt DESC")
    List<SupportMessage> search(@Param("email") String email, @Param("date") LocalDate date);
}
