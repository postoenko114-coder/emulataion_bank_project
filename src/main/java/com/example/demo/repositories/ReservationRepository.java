package com.example.demo.repositories;

import com.example.demo.models.branch.reservation.Reservation;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {

    @Query("SELECT COUNT(r) FROM Reservation r WHERE " +
            "r.bankBranch.id = :branchId AND " +
            "r.bankService.id = :serviceId AND " +
            "CAST(r.startReservation AS date) = :date AND " +
            "r.status = 'ACTIVE'")
    Long countBookedSlots(@Param("branchId") Long branchId,
                          @Param("serviceId") Long serviceId,
                          @Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE " +
            "r.bankService.id = :resourceId AND " +
            "r.startReservation < :targetEnd AND r.endReservation > :targetStart AND " +
            "r.status = 'ACTIVE'")
    List<Reservation> findReservationsForDay(@Param("resourceId") Long serviceId,
                                             @Param("targetStart") LocalDateTime targetStart,
                                             @Param("targetEnd") LocalDateTime targetEnd);

    // Проверка на пересечение (оверлаппинг)
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.bankService.id = :serviceId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.startReservation < :endReservation AND r.endReservation > :startReservation")
    boolean existsOverlappingReservation(@Param("serviceId") Long serviceId,
                                         @Param("startReservation") LocalDateTime startReservation,
                                         @Param("endReservation") LocalDateTime endReservation);

    @Query("SELECT r FROM Reservation r WHERE " +
            "(:branchId IS NULL OR r.bankBranch.id = :branchId) AND " +
            "(:serviceId IS NULL OR r.bankService.id = :serviceId) AND " +
            "(:date IS NULL OR CAST(r.startReservation AS LocalDate) = :date)")
    List<Reservation> findReservationsByFilters(Long branchId, Long serviceId, LocalDate date);
}
