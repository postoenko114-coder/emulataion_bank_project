package com.example.demo.dto;

public class WorkingHourDTO {

    private String dayOfWeek;

    private String openTime;
    private String closeTime;

    public WorkingHourDTO(String dayOfWeek, String openTime, String closeTime) {
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public WorkingHourDTO() {}

    public String getCloseTime() {return closeTime;}

    public void setCloseTime(String closeTime) {this.closeTime = closeTime;}

    public String getDayOfWeek() {return dayOfWeek;}

    public void setDayOfWeek(String dayOfWeek) {this.dayOfWeek = dayOfWeek;}

    public String getOpenTime() {return openTime;}

    public void setOpenTime(String openTime) {this.openTime = openTime;}
}