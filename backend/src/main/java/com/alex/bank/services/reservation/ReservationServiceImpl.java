package com.alex.bank.services.reservation;

import com.alex.bank.dto.ReservationDTO;
import com.alex.bank.mapper.ReservationMapper;
import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.models.branch.reservation.Reservation;
import com.alex.bank.models.branch.reservation.StatusReservation;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.ReservationRepository;
import com.alex.bank.repositories.UserRepository;
import com.alex.bank.services.bankBranch.BankBranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;

    private final UserRepository userRepository;

    private final BankBranchService bankBranchService;

    private final ReservationMapper reservationMapper;

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
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created reservationId={} userId={} branchId={} serviceId={} start={} end={}",
                saved.getId(), user_id, bankBranch.getId(), bankService.getId(), startReservation, endReservation);
        return reservationMapper.toDTO(saved);
    }

    @Transactional
    @Override
    public List<ReservationDTO> getAllReservations(){
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public ReservationDTO getReservationById(Long reservation_id){
        Reservation reservation = reservationRepository.findById(reservation_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        return reservationMapper.toDTO(reservation);
    }

    @Transactional
    @Override
    public List<ReservationDTO> getAllReservationsOfUser(Long user_id){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getReservations().stream()
                .map(reservationMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public ReservationDTO cancelReservation(Long reservation_id){
        Reservation reservation = reservationRepository.findById(reservation_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        reservation.setStatus(StatusReservation.CANCELLED);
        log.info("Reservation cancelled reservationId={} userId={}", reservation.getId(), reservation.getUser().getId());
        return reservationMapper.toDTO(reservation);
    }

    @Transactional
    @Override
    public ReservationDTO completeReservation(Long reservation_id){
        Reservation reservation= reservationRepository.findById(reservation_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        reservation.setStatus(StatusReservation.COMPLETED);
        log.info("Reservation completed reservationId={} userId={}", reservation.getId(), reservation.getUser().getId());
        return reservationMapper.toDTO(reservation);
    }

    @Transactional
    @Override
    public List<ReservationDTO> findReservationsByServiceAndDateForBranch(Long bankBranch_id, Long bankService_id, LocalDate date) {
        LocalDateTime startDay = date.atStartOfDay();
        LocalDateTime endDay = date.atStartOfDay().plusDays(1);

        return reservationRepository.findReservationsByFilters(bankBranch_id, bankService_id, startDay, endDay).stream()
                .map(reservationMapper::toDTO)
                .toList();
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
