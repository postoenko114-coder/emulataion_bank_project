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
            "r.status = 'ACTIVE' AND " +
            "r.startReservation BETWEEN :startOfDay AND :endOfDay")
    Long countBookedSlots(@Param("branchId") Long branchId,
                          @Param("serviceId") Long serviceId,
                          @Param("startOfDay") LocalDateTime startOfDay,
                          @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.bankService.id = :serviceId " +
            "AND r.status = 'ACTIVE' " +
            "AND r.startReservation < :endReservation AND r.endReservation > :startReservation")
    boolean existsOverlappingReservation(@Param("serviceId") Long serviceId,
                                         @Param("startReservation") LocalDateTime startReservation,
                                         @Param("endReservation") LocalDateTime endReservation);

    @Query("SELECT r FROM Reservation r WHERE " +
                  "(:branchId IS NULL OR r.bankBranch.id = :branchId) AND " +
                  "(:serviceId IS NULL OR r.bankService.id = :serviceId) AND " +
            "(CAST(:startOfDay as timestamp) IS NULL OR r.startReservation >= :startOfDay) AND " +
            "(CAST(:endOfDay as timestamp) IS NULL OR r.startReservation <= :endOfDay)")
    List<Reservation> findReservationsByFilters(
            @Param("branchId") Long branchId,
            @Param("serviceId") Long serviceId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
