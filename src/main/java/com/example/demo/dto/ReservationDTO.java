package com.example.demo.dto;

import com.example.demo.models.branch.reservation.StatusReservation;

import java.time.Instant;
import java.time.LocalDateTime;


public class ReservationDTO {
    private Long id;

    private LocalDateTime startReservation;

    private String serviceName;
    private String branchName;
    private String username;

    private LocalDateTime endReservation;

    private StatusReservation statusReservation;

    public ReservationDTO(Long id, LocalDateTime startReservation, LocalDateTime endReservation,  StatusReservation statusReservation,  String serviceName, String branchName, String username) {
        this.id = id;
        this.startReservation = startReservation;
        this.endReservation = endReservation;
        this.statusReservation = statusReservation;
        this.serviceName = serviceName;
        this.branchName = branchName;
        this.username = username;
    }

    public ReservationDTO() {}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public StatusReservation getStatusReservation() {return statusReservation;}

    public void setStatusReservation(StatusReservation statusReservation) {this.statusReservation = statusReservation;}

    public LocalDateTime getStartReservation() {return startReservation;}

    public void setStartReservation(LocalDateTime startReservation) {this.startReservation = startReservation;}

    public LocalDateTime getEndReservation() {return endReservation;}

    public void setEndReservation(LocalDateTime endReservation) {this.endReservation = endReservation;}

    public String getServiceName() {return serviceName;}

    public void setServiceName(String serviceName) {this.serviceName = serviceName;}

    public String getBranchName() {return branchName;}

    public void setBranchName(String branchName) {this.branchName = branchName;}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}
}
