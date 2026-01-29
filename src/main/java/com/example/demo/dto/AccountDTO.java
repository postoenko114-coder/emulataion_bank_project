package com.example.demo.dto;

import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.account.StatusAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDTO {
    private Long id;

    private String accountNumber;

    private CurrencyAccount currency;

    private BigDecimal balance;

    private StatusAccount statusAccount;

    private LocalDateTime createdAt;

    public AccountDTO(){}

    public AccountDTO(Long id, String accountNumber, CurrencyAccount currency, BigDecimal balance, StatusAccount statusAccount, LocalDateTime createdAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = balance;
        this.statusAccount = statusAccount;
        this.createdAt = createdAt;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getAccountNumber() {return accountNumber;}

    public void setAccountNumber(String accountNumber) {this.accountNumber = accountNumber;}

    public BigDecimal getBalance() {return balance;}

    public void setBalance(BigDecimal balance) {this.balance = balance;}

    public CurrencyAccount getCurrency() {return currency;}

    public void setCurrency(CurrencyAccount currency) {this.currency = currency;}

    public StatusAccount getStatusAccount() {return statusAccount;}

    public void setStatusAccount(StatusAccount statusAccount) {this.statusAccount = statusAccount;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}
