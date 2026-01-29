package com.example.demo.repositories;

import com.example.demo.models.transaction.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.accountFrom af " +
            "LEFT JOIN t.accountTo at " +
            "WHERE (af.user.id = :userId) OR (at.user.id = :userId) " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
                   "LEFT JOIN t.accountFrom af " +
                   "LEFT JOIN t.accountTo at " +
                   "WHERE ((af.user.id = :userId) OR (at.user.id = :userId)) AND CAST(t.createdAt AS date) > :date")
    List<Transaction> findTransactionsForUserAfterDate(@Param("userId") Long userId,
                                                       @Param("date") LocalDate date,
                                                       Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.accountFrom af " +
            "LEFT JOIN t.accountTo at " +
            "WHERE ((af.user.id = :userId) OR (at.user.id = :userId)) AND CAST(t.createdAt AS date) < :date")
    List<Transaction> findHistoryForUserBeforeDate(@Param("userId") Long userId,
                                                   @Param("date") LocalDate date,
                                                   Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.accountFrom af " +
            "LEFT JOIN t.accountTo at " +
            "WHERE ((af.user.id = :userId) OR (at.user.id = :userId)) AND CAST(t.createdAt AS date) = :date")
    List<Transaction> findHistoryForUserInDate(@Param("userId") Long userId,
                                               @Param("date") LocalDate date,
                                               Pageable pageable);
    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.accountFrom af " +
            "LEFT JOIN t.accountTo at " +
            "WHERE ((af.user.id = :userId) OR (at.user.id = :userId)) AND t.amount = :amount")
    List<Transaction> findByUserIdAndAmount(Long userId, BigDecimal amount, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.accountFrom af " +
            "LEFT JOIN t.accountTo at " +
            "WHERE ((af.user.id = :userId) OR (at.user.id = :userId)) AND t.isHidden = false")
    List<Transaction> findAllVisibleByUser(@Param("userId") Long userId, Pageable pageable);
}
