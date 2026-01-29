package com.example.demo.dto.transaction;

import com.example.demo.models.transaction.StatusTransaction;
import com.example.demo.models.transaction.TypeTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTOAdmin extends TransactionDTO {
    private Boolean isHiddenByUser;

    public TransactionDTOAdmin(Long id, TypeTransaction typeTransaction, StatusTransaction statusTransaction, String accountTo, String accountFrom, BigDecimal amount, String description, LocalDateTime createdAt, Boolean isHiddenByUser) {
        super(id, typeTransaction,statusTransaction, accountTo, accountFrom, amount, description, createdAt);
        this.isHiddenByUser = isHiddenByUser;
    }

    public TransactionDTOAdmin() {}

    public Boolean getHiddenByUser() {return isHiddenByUser;}

    public void setHiddenByUser(Boolean hiddenByUser) {isHiddenByUser = hiddenByUser;}
}
