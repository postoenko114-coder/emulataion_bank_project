package com.example.demo.models.branch;

import com.example.demo.dto.BankServiceDTO;
import com.example.demo.models.branch.reservation.Reservation;
import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "services")
public class BankService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankServiceName;

    private String duration;

    private String description;

    @ManyToMany
    @JoinTable(name = "bankBranch_bankService",
            joinColumns = @JoinColumn(name = "bankBranch_id"),
            inverseJoinColumns = @JoinColumn(name = "bankService_id"))
    private Set<BankBranch> bankBranches ;

    @OneToMany(mappedBy = "bankService",  cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Reservation> reservations;

    public BankService() {}

    public BankService(String bankServiceName, String duration,  String description) {
        this.bankServiceName = bankServiceName;
        this.duration = duration;
        this.description = description;
    }

    public BankServiceDTO toDTO() {
        BankServiceDTO bankServiceDTO = new BankServiceDTO();
        bankServiceDTO.setId(id);
        bankServiceDTO.setBankServiceName(bankServiceName);
        bankServiceDTO.setDuration(duration);
        bankServiceDTO.setDescription(description);
        return bankServiceDTO;
    }

    public List<Reservation> getReservations() {return reservations;}

    public void setReservations(List<Reservation> reservations) {this.reservations = reservations;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getDuration() {return duration;}

    public void setDuration(String duration) {this.duration = duration;}

    public String getBankServiceName() {return bankServiceName;}

    public void setBankServiceName(String bankServiceName) {this.bankServiceName = bankServiceName;}

    public Set<BankBranch> getBankBranches() {return bankBranches;}

    public void setBankBranches(Set<BankBranch> bankBranches) {this.bankBranches = bankBranches;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}
}
