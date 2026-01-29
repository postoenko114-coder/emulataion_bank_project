package com.example.demo.controllers.admin;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import com.example.demo.services.reservation.ReservationService;
import com.example.demo.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reservations")
public class AdminReservationController {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private BankBranchService bankBranchService;
    @Autowired
    private BankServiceService bankServiceService;
    @Autowired
    private UserService userService;

    public AdminReservationController(ReservationService reservationService,  BankBranchService bankBranchService, BankServiceService bankServiceService,  UserService userService) {
        this.reservationService = reservationService;
        this.bankBranchService = bankBranchService;
        this.bankServiceService = bankServiceService;
        this.userService = userService;
    }

    @GetMapping
    public List<ReservationDTO> getReservations(){
        return reservationService.getAllReservations();
    }

    @GetMapping("/search")
    public List<ReservationDTO> getReservationsByFilters(
            @RequestParam(required = false) String branchName,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) LocalDate date) {

        Long branchId = null;
        Long serviceId = null;

        if (branchName != null && !branchName.isEmpty()) {
            BankBranch branch = bankBranchService.findBranchByName(branchName);
            if (branch != null) branchId = branch.getId();
        }

        if (serviceName != null && !serviceName.isEmpty()) {
            BankService service = bankServiceService.findServiceByName(serviceName).get(0);
            if (service != null) serviceId = service.getId();
        }

        return reservationService.findReservationsByServiceAndDateForBranch(branchId, serviceId, date);
    }

    @PostMapping("/{reservationId}/cancelReservation")
    public ResponseEntity<String> cancelReservation( @PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok("Reservation cancelled");
    }

    @PostMapping("/{reservationId}/completeReservation")
    public ResponseEntity<String> markAsCompleteReservation(@PathVariable Long reservationId) {
        reservationService.completeReservation(reservationId);
        return ResponseEntity.ok("Reservation completed");
    }

    @PostMapping
    public ReservationDTO createReservation(@RequestParam String username, @RequestParam LocalDateTime startReservation, @RequestParam String serviceName, @RequestParam String branchName) {
        BankService bankService = bankServiceService.findServiceByName(serviceName).get(0);
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        return reservationService.addReservation(startReservation, userService.findUserByUsername(username).getId(), bankService, bankBranch);
    }

}
