package com.example.demo.dto;

import java.util.List;

public class BankBranchDTO {
    private Long id;

    private String bankBranchName;

    private LocationDTO locationDTO;

    private List<WorkingHourDTO> schedule;

    private List<BankServiceDTO> bankServices;

    public BankBranchDTO() {}

    public BankBranchDTO(Long id, String bankBranchName, LocationDTO locationDTO, List<WorkingHourDTO> schedule, List<BankServiceDTO> bankServices) {
        this.id = id;
        this.bankBranchName = bankBranchName;
        this.locationDTO = locationDTO;
        this.schedule = schedule;
        this.bankServices = bankServices;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getBankBranchName() {return bankBranchName;}

    public void setBankBranchName(String bankBranchName) {this.bankBranchName = bankBranchName;}

    public LocationDTO getLocationDTO() {return locationDTO;}

    public void setLocationDTO(LocationDTO locationDTO) {this.locationDTO = locationDTO;}

    public List<WorkingHourDTO> getSchedule() {return schedule;}

    public void setSchedule(List<WorkingHourDTO> schedule) {this.schedule = schedule;}

    public List<BankServiceDTO> getBankServices() {return bankServices;}

    public void setBankServices(List<BankServiceDTO> bankServices) {this.bankServices = bankServices;}

}
