package com.example.demo.dto.transaction;

import com.example.demo.models.transaction.StatusTransaction;
import com.example.demo.models.transaction.TypeTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;

    private TypeTransaction typeTransaction;

    private StatusTransaction statusTransaction;

    private String accountTo;

    private String accountFrom;

    private BigDecimal amount;

    private String description;

    private LocalDateTime createdAt;

    public TransactionDTO () {}

    public TransactionDTO (Long id, TypeTransaction typeTransaction, StatusTransaction statusTransaction, String accountTo, String accountFrom, BigDecimal amount, String description, LocalDateTime createdAt) {
        this.id = id;
        this.typeTransaction = typeTransaction;
        this.statusTransaction = statusTransaction;
        this.accountTo = accountTo;
        this.accountFrom = accountFrom;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getAccountFrom() {return accountFrom;}

    public void setAccountFrom(String accountFrom) {this.accountFrom = accountFrom;}

    public String getAccountTo() {return accountTo;}

    public void setAccountTo(String accountTo) {this.accountTo = accountTo;}

    public BigDecimal getAmount() {return amount;}

    public void setAmount(BigDecimal amount) {this.amount = amount;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public TypeTransaction getTypeTransaction() {return typeTransaction;}

    public void setTypeTransaction(TypeTransaction typeTransaction) {this.typeTransaction = typeTransaction;}

    public StatusTransaction getStatusTransaction() {return statusTransaction;}

    public void setStatusTransaction(StatusTransaction statusTransaction) {this.statusTransaction = statusTransaction;}
}
