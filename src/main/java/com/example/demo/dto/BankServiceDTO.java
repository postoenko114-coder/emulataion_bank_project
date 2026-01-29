package com.example.demo.dto;

public class BankServiceDTO {
    private Long id;

    private String bankServiceName;

    private String duration;

    private String description;

    public BankServiceDTO(Long id, String bankServiceName, String duration,  String description) {
        this.id = id;
        this.bankServiceName = bankServiceName;
        this.duration = duration;
        this.description = description;
    }

    public BankServiceDTO() {}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getBankServiceName() {return bankServiceName;}

    public void setBankServiceName(String bankServiceName) {
        this.bankServiceName = bankServiceName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}
}
