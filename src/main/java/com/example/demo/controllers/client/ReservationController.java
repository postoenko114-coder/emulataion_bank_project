package com.example.demo.controllers.client;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import com.example.demo.services.reservation.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private BankServiceService bankServiceService;
    @Autowired
    private BankBranchService bankBranchService;

    public ReservationController(ReservationService reservationService,  BankServiceService bankServiceService,  BankBranchService bankBranchService) {
        this.reservationService = reservationService;
        this.bankServiceService = bankServiceService;
        this.bankBranchService = bankBranchService;
    }

    @GetMapping
    public List<ReservationDTO> getReservations(@PathVariable Long userId) {
        return reservationService.getAllReservationsOfUser(userId);
    }

    @GetMapping("/{reservationId}")
    public ReservationDTO getReservation( @PathVariable Long reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    @PostMapping
    public ReservationDTO createReservation(@PathVariable Long userId, @RequestParam LocalDateTime startReservation, @RequestParam String serviceName, @RequestParam String branchName) {
        BankService bankService = bankServiceService.findServiceByName(serviceName).get(0);
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        return reservationService.addReservation(startReservation, userId, bankService, bankBranch);
    }

    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok("Reservation has been cancelled");
    }

}
