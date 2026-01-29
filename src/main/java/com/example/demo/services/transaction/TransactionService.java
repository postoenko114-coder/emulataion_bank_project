package com.example.demo.services.transaction;


import com.example.demo.models.account.Account;
import com.example.demo.models.transaction.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    @Transactional
    List<Transaction> getListAccountTransactions(Long account_id, Pageable pageable);

    @Transactional
    List<Transaction> getListUserTransaction(Long user_id, Pageable pageable);

    @Transactional
    List<Transaction> getListUserTransactionForAdmin(Long user_id, Pageable pageable);

    @Transactional
    Transaction recordDeposit(Account account, BigDecimal amount);

    @Transactional
    Transaction recordWithdrawal(Account account, BigDecimal amount);

    @Transactional
    Transaction recordTransfer(Account accountTo, Account accountFrom, BigDecimal amount, String description);

    @Transactional
    Transaction recordPaymentByCard(Account account, BigDecimal amount);

    @Transactional
    Transaction getTransactionById(Long transaction_id);

    @Transactional
    List<Transaction> getListAccountTransactionsByAmount(Long account_id, BigDecimal amount, Pageable pageable);

    @Transactional
    List<Transaction> getListAccountTransactionsInDate(Long account_id, LocalDate date, Pageable pageable);

    @Transactional
    List<Transaction> getListAccountTransactionsBeforeDate(Long account_id, LocalDate date, Pageable pageable);

    @Transactional
    List<Transaction> getListAccountTransactionsAfterDate(Long account_id, LocalDate date, Pageable pageable);

    @Transactional
    void hideTransaction(Long transaction_id);

    @Transactional
    void cancelTransaction(Long transactionId);
}
