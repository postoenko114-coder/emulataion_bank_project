package com.example.demo.models.account;

import com.example.demo.dto.AccountDTO;
import com.example.demo.models.card.Card;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private CurrencyAccount currencyAccount;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private StatusAccount statusAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "accountTo",  cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Transaction> transactionsTo;

    @OneToMany(mappedBy = "accountFrom",  cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Transaction> transactionsFrom;

    @OneToOne(cascade = CascadeType.ALL)
    private Card card;

    private LocalDateTime createdAt;

    public Account() {}

    public Account(String accountNumber, CurrencyAccount currencyAccount, BigDecimal balance, StatusAccount statusAccount, User user) {
        this.accountNumber = accountNumber;
        this.currencyAccount = currencyAccount;
        this.balance = balance;
        this.statusAccount = statusAccount;
        this.createdAt = LocalDateTime.now();
        this.user = user;
    }

    public AccountDTO toDTO(){
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(id);
        accountDTO.setAccountNumber(accountNumber);
        accountDTO.setCurrency(currencyAccount);
        accountDTO.setBalance(balance);
        accountDTO.setStatusAccount(statusAccount);
        accountDTO.setCreatedAt(createdAt);
        return accountDTO;
    }

    public String getAccountNumber() {return accountNumber;}

    public void setAccountNumber(String accountNumber) {this.accountNumber = accountNumber;}

    public BigDecimal getBalance() {return balance;}

    public void setBalance(BigDecimal balance) {this.balance = balance;}

    public Card getCard() {return card;}

    public void setCard(Card card) {this.card = card;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public CurrencyAccount getCurrencyAccount() {return currencyAccount;}

    public void setCurrencyAccount(CurrencyAccount currencyAccount) {this.currencyAccount = currencyAccount;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public StatusAccount getStatusAccount() {return statusAccount;}

    public void setStatusAccount(StatusAccount statusAccount) {this.statusAccount = statusAccount;}

    public List<Transaction> getTransactionsFrom() {return transactionsFrom;}

    public void setTransactionsFrom(List<Transaction> transactionsFrom) {this.transactionsFrom = transactionsFrom;}

    public List<Transaction> getTransactionsTo() {return transactionsTo;}

    public void setTransactionsTo(List<Transaction> transactionsTo) {this.transactionsTo = transactionsTo;}

    public User getUser() {return user;}

    public void setUser(User user) {this.user = user;}
}
