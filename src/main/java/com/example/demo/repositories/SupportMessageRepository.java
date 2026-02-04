package com.example.demo.repositories;

import com.example.demo.models.supportMessage.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    Optional<SupportMessage> findSupportMessageById(Long id);

    @Query("SELECT m FROM SupportMessage m WHERE " +
            "(:email IS NULL OR UPPER(m.userEmail) LIKE UPPER(CONCAT('%', :email, '%'))) AND " +
            "(CAST(:startOfDay as timestamp) IS NULL OR m.createdAt >= :startOfDay) AND " +
            "(CAST(:endOfDay as timestamp) IS NULL OR m.createdAt <= :endOfDay)")
    List<SupportMessage> search(@Param("email") String email, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
