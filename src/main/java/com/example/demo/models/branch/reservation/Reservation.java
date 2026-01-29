package com.example.demo.models.branch.reservation;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startReservation;

    private LocalDateTime endReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bankBranch_id")
    @JsonIgnore
    private BankBranch bankBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    @JsonIgnore
    private BankService bankService;

    @Enumerated(EnumType.STRING)
    private StatusReservation status;

    public Reservation() {}

    public Reservation( LocalDateTime startReservation, LocalDateTime endReservation, StatusReservation status) {
        this.startReservation = startReservation;
        this.endReservation = endReservation;
        this.status = status;
    }

    public ReservationDTO toDTO(){
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setId(id);
        reservationDTO.setStartReservation(startReservation);
        reservationDTO.setEndReservation(endReservation);
        reservationDTO.setStatusReservation(status);
        reservationDTO.setBranchName(bankBranch.getBankBranchName());
        reservationDTO.setServiceName(bankService.getBankServiceName());
        reservationDTO.setUsername(user.getRealUsername());
        return  reservationDTO;
    }

    public BankBranch getBankBranch() {return bankBranch;}

    public void setBankBranch(BankBranch bankBranch) {this.bankBranch = bankBranch;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public BankService getBankService() {return bankService;}

    public void setBankService(BankService bankService) {this.bankService = bankService;}

    public StatusReservation getStatus() {return status;}

    public void setStatus(StatusReservation status) {this.status = status;}

    public User getUser() {return user;}

    public void setUser(User user) {this.user = user;}

    public LocalDateTime getStartReservation() {return startReservation;}

    public void setStartReservation(LocalDateTime startReservation) {this.startReservation = startReservation;}

    public LocalDateTime getEndReservation() {return endReservation;}

    public void setEndReservation(LocalDateTime endReservation) {this.endReservation = endReservation;}

}
