package com.example.demo.services.reservation;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.models.branch.reservation.Reservation;
import com.example.demo.models.branch.reservation.StatusReservation;
import com.example.demo.models.user.User;
import com.example.demo.repositories.BankServiceRepository;
import com.example.demo.repositories.ReservationRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.bankBranch.BankBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    BankServiceRepository bankServiceRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BankBranchService bankBranchService;

    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    @Override
    public ReservationDTO addReservation(LocalDateTime startReservation, Long user_id, BankService bankService, BankBranch bankBranch) {
        if (startReservation.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book in the past");
        }
        if (!bankBranchService.isBranchOpen(bankBranch.getId(), startReservation)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Branch is not open in this time");
        }

        LocalDateTime endReservation = calculateEndTime(startReservation, bankService.getDuration());
        if(!bankBranchService.isBranchOpen(bankBranch.getId(), endReservation)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It's too late, you need choose time early!");
        }

        boolean isBusy = reservationRepository.existsOverlappingReservation(
                bankService.getId(),
                startReservation,
                endReservation
        );
        if (isBusy) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This time slot is already taken");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(userRepository.findById(user_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
        reservation.setStartReservation(startReservation);
        reservation.setEndReservation(endReservation);
        reservation.setStatus(StatusReservation.ACTIVE);
        reservation.setBankService(bankService);
        reservation.setBankBranch(bankBranch);
        reservationRepository.save(reservation);
        return reservation.toDTO();
    }

    @Transactional
    @Override
    public List<ReservationDTO> getAllReservations(){
        List<Reservation> reservations = reservationRepository.findAll();
        List<ReservationDTO> dtos = new ArrayList<>();
        for (Reservation reservation : reservations) {
            dtos.add(reservation.toDTO());
        }
        return dtos;
    }

    @Transactional
    @Override
    public ReservationDTO getReservationById(Long reservation_id){
        Reservation reservation = reservationRepository.findById(reservation_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        return reservation.toDTO();
    }

    @Transactional
    @Override
    public List<ReservationDTO> getAllReservationsOfUser(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Reservation> reservations = user.getReservations();
        List<ReservationDTO> reservationDTOs = new ArrayList<>();
        for(Reservation reservation : reservations){
            reservationDTOs.add(reservation.toDTO());
        }
        return reservationDTOs;
    }

    @Transactional
    @Override
    public void cancelReservation(Long reservation_id){
        Reservation reservation = reservationRepository.findById(reservation_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        reservation.setStatus(StatusReservation.CANCELLED);
    }

    @Transactional
    @Override
    public void completeReservation(Long reservation_id){
        Reservation reservation= reservationRepository.findById(reservation_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        reservation.setStatus(StatusReservation.COMPLETED);
    }

    @Transactional
    @Override
    public List<ReservationDTO> findReservationsByServiceAndDateForBranch(Long bankBranch_id, Long bankService_id, LocalDate date) {
        LocalDateTime startDay = date.atStartOfDay();
        LocalDateTime endDay = date.atStartOfDay().plusDays(1);

        List<Reservation> reservations = reservationRepository.findReservationsByFilters(bankBranch_id, bankService_id, startDay, endDay);
        List<ReservationDTO> reservationDTOs = new ArrayList<>();
        for (Reservation reservation : reservations) {
            reservationDTOs.add(reservation.toDTO());
        }
        return reservationDTOs;
    }

    private LocalDateTime calculateEndTime(LocalDateTime start, String durationStr) {
        if (durationStr == null || durationStr.isBlank()) {
            return start;
        }

        long totalMinutes = 0;
        String input = durationStr.toLowerCase();
        boolean foundUnit = false;

        Pattern hourPattern = Pattern.compile("(\\d+)\\s*(h|hour|hours|hr|час|часа|часов)");
        Matcher hourMatcher = hourPattern.matcher(input);

        if (hourMatcher.find()) {
            long hours = Long.parseLong(hourMatcher.group(1));
            totalMinutes += hours * 60;
            foundUnit = true;
        }

        Pattern minutePattern = Pattern.compile("(\\d+)\\s*(m|min|minute|minutes|мин|минуты|минут)");
        Matcher minuteMatcher = minutePattern.matcher(input);
        if (minuteMatcher.find()) {
            long minutes = Long.parseLong(minuteMatcher.group(1));
            totalMinutes += minutes;
            foundUnit = true;
        }

        if (!foundUnit) {
            String digitsOnly = durationStr.replaceAll("[^0-9]", "");
            if (!digitsOnly.isEmpty()) {
                totalMinutes = Long.parseLong(digitsOnly);
            }
        }

        return start.plusMinutes(totalMinutes);
    }
}
