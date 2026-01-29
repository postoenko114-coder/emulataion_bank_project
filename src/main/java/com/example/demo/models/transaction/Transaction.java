package com.example.demo.models.transaction;

import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.dto.transaction.TransactionDTOAdmin;
import com.example.demo.models.account.Account;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeTransaction typeTransaction;

    @Enumerated(EnumType.STRING)
    private StatusTransaction statusTransaction;

    private BigDecimal amount;

    private String description;

    @ManyToOne
    @JoinColumn(name = "accountFrom_id")
    private Account accountFrom;

    @ManyToOne
    @JoinColumn(name = "accountTo_id")
    private Account accountTo;

    private LocalDateTime createdAt;

    private Boolean isHidden = false;

    public Transaction() {}

    public Transaction(Long id, TypeTransaction typeTransaction, StatusTransaction statusTransaction, Account accountFrom, Account accountTo, BigDecimal amount, String description) {
        this.id = id;
        this.typeTransaction = typeTransaction;
        this.statusTransaction = statusTransaction;
        this.accountFrom = accountFrom;
        this.accountTo = accountTo;
        this.amount = amount;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public TransactionDTO toDTO() {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setId(id);
        transactionDTO.setTypeTransaction(typeTransaction);
        transactionDTO.setStatusTransaction(statusTransaction);
        transactionDTO.setAccountFrom(accountFrom != null ? accountFrom.getAccountNumber() : "Bank/ATM");
        transactionDTO.setAccountTo(accountTo != null ? accountTo.getAccountNumber() : "Bank/ATM");
        transactionDTO.setAmount(amount);
        transactionDTO.setDescription(description);
        transactionDTO.setCreatedAt(createdAt);
        return transactionDTO;
    }

    public TransactionDTOAdmin toDTOAdmin() {
        TransactionDTOAdmin transactionDTOAdmin = new TransactionDTOAdmin();
        transactionDTOAdmin.setId(id);
        transactionDTOAdmin.setTypeTransaction(typeTransaction);
        transactionDTOAdmin.setStatusTransaction(statusTransaction);
        transactionDTOAdmin.setAccountFrom(accountFrom != null ? accountFrom.getAccountNumber() : "Bank/ATM");
        transactionDTOAdmin.setAccountTo(accountTo != null ? accountTo.getAccountNumber() : "Bank/ATM");
        transactionDTOAdmin.setAmount(amount);
        transactionDTOAdmin.setDescription(description);
        transactionDTOAdmin.setCreatedAt(createdAt);
        transactionDTOAdmin.setHiddenByUser(isHidden);
        return transactionDTOAdmin;
    }

    public Account getAccountFrom() {return accountFrom;}

    public void setAccountFrom(Account accountFrom) {this.accountFrom = accountFrom;}

    public Account getAccountTo() {return accountTo;}

    public void setAccountTo(Account accountTo) {this.accountTo = accountTo;}

    public BigDecimal getAmount() {return amount;}

    public void setAmount(BigDecimal amount) {this.amount = amount;}

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public Boolean getHidden() {return isHidden;}

    public void setHidden(Boolean hidden) {isHidden = hidden;}

    public StatusTransaction getStatusTransaction() {return statusTransaction;}

    public void setStatusTransaction(StatusTransaction statusTransaction) {this.statusTransaction = statusTransaction;}

    public TypeTransaction getTypeTransaction() {return typeTransaction;}

    public void setTypeTransaction(TypeTransaction typeTransaction) {this.typeTransaction = typeTransaction;}
}
