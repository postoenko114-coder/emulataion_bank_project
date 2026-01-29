package com.example.demo.models.branch;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

@Embeddable
public class WorkingHour {

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    private LocalTime openTime;
    private LocalTime closeTime;

    public WorkingHour() {
    }

    public WorkingHour(DayOfWeek day, LocalTime openTime, LocalTime closeTime) {
        this.day = day;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public LocalTime getCloseTime() {return closeTime;}

    public void setCloseTime(LocalTime closeTime) {this.closeTime = closeTime;}

    public DayOfWeek getDayOfWeek() {return day;}

    public void setDayOfWeek(DayOfWeek day) {this.day = day;}

    public LocalTime getOpenTime() {return openTime;}

    public void setOpenTime(LocalTime openTime) {this.openTime = openTime;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkingHour that = (WorkingHour) o;
        return day == that.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day);
    }
}
