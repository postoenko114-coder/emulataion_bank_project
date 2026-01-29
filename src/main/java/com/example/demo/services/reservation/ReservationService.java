package com.example.demo.services.reservation;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public interface ReservationService {

    @Transactional
    ReservationDTO addReservation(LocalDateTime startReservation, Long user_id, BankService bankService, BankBranch bankBranch);

    @Transactional
    List<ReservationDTO> getAllReservations();

    @Transactional
    ReservationDTO getReservationById(Long reservation_id);

    @Transactional
    List<ReservationDTO> getAllReservationsOfUser(Long user_id);

    @Transactional
    void cancelReservation(Long reservation_id);

    @Transactional
    void completeReservation(Long reservation_id);

    @Transactional
    List<ReservationDTO> findReservationsByServiceAndDateForBranch(Long bankBranch_id, Long bankService_id, LocalDate date);
}
