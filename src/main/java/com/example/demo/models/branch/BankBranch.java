package com.example.demo.models.branch;

import com.example.demo.dto.BankBranchDTO;
import com.example.demo.dto.BankServiceDTO;
import com.example.demo.dto.WorkingHourDTO;
import com.example.demo.models.branch.reservation.Reservation;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "bank_branches")
public class BankBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankBranchName;

    @Embedded
    private Location location;

    @ManyToMany(mappedBy = "bankBranches")
    private Set<BankService> bankServices;

    @OneToMany(mappedBy = "bankBranch", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Reservation> reservations;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "branch_schedule", joinColumns = @JoinColumn(name = "branch_id"))
    private Set<WorkingHour> schedule = new HashSet<>();


    public BankBranch() {
    }

    public BankBranch(String bankBranchName, Location location) {
        this.location = location;
        this.bankBranchName = bankBranchName;
    }

    public BankBranchDTO toDTO() {
        BankBranchDTO dto = new BankBranchDTO();
        dto.setId(id);
        dto.setBankBranchName(bankBranchName);
        dto.setLocationDTO(location.toDTO());

        List<WorkingHourDTO> scheduleDto = schedule.stream()
                .sorted(Comparator.comparing(WorkingHour::getDayOfWeek))
                .map(wh -> new WorkingHourDTO(
                        wh.getDayOfWeek().name(),
                        wh.getOpenTime().toString(),
                        wh.getCloseTime().toString()
                ))
                .toList();

        List<BankServiceDTO> bankServiceDTOS = new ArrayList<>();
        for (BankService bankService : bankServices) {
            bankServiceDTOS.add(bankService.toDTO());
        }

        dto.setBankServices(bankServiceDTOS);
        dto.setSchedule(scheduleDto);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBankBranchName() {return bankBranchName;}

    public void setBankBranchName(String bankBranchName) {this.bankBranchName = bankBranchName;}

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Set<BankService> getBankServices() {
        return bankServices;
    }

    public void setServices(Set<BankService> bankServices) {
        this.bankServices = bankServices;
    }

    public Set<WorkingHour> getSchedule() {return schedule;}

    public void setSchedule(Set<WorkingHour> schedule) {this.schedule = schedule;}

}
