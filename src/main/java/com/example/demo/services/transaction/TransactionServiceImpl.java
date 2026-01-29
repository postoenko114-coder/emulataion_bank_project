package com.example.demo.services.transaction;

import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.models.account.Account;
import com.example.demo.models.transaction.StatusTransaction;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.models.transaction.TypeTransaction;
import com.example.demo.models.user.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.TransactionRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository,  UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactions(Long account_id, Pageable pageable) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        List<Transaction> transactions = transactionRepository.findAllVisibleByUser(account.getUser().getId(), pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListUserTransaction(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
        List<Transaction> transactions = transactionRepository.findAllVisibleByUser(user_id, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListUserTransactionForAdmin(Long user_id, Pageable pageable){
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
        List<Transaction> transactions = transactionRepository.findAllByUserId(user_id, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public Transaction recordDeposit(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.DEPOSIT);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountTo(account);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        return transaction;
    }

    @Transactional
    @Override
    public Transaction recordWithdrawal(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.WITHDRAWAL);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountFrom(account);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        return transaction;
    }

    @Transactional
    @Override
    public Transaction recordTransfer(Account accountTo, Account accountFrom, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.TRANSFER);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountTo(accountTo);
        transaction.setAccountFrom(accountFrom);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setDescription(description);
        transactionRepository.save(transaction);
        accountTo.getTransactionsFrom().add(transaction);
        accountFrom.getTransactionsFrom().add(transaction);
        return transaction;
    }

    @Transactional
    @Override
    public Transaction recordPaymentByCard(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(TypeTransaction.CARD);
        transaction.setStatusTransaction(StatusTransaction.SUCCESS);
        transaction.setAmount(amount);
        transaction.setAccountFrom(account);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        account.getTransactionsFrom().add(transaction);
        return transaction;
    }

    @Transactional
    @Override
    public Transaction getTransactionById(Long transaction_id) {
        Transaction transaction = transactionRepository.findById(transaction_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        return transaction;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactionsByAmount(Long account_id, BigDecimal amount, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndAmount(account_id, amount, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactionsInDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findHistoryForUserInDate(account_id, date, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactionsBeforeDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findHistoryForUserBeforeDate(account_id, date, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public List<Transaction> getListAccountTransactionsAfterDate(Long account_id, LocalDate date, Pageable pageable) {
        List<Transaction> transactions = transactionRepository.findTransactionsForUserAfterDate(account_id, date, pageable);
        return transactions;
    }

    @Transactional
    @Override
    public void hideTransaction(Long transaction_id) {
        Transaction transaction = transactionRepository.findById(transaction_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        transaction.setHidden(true);

    }

    @Transactional
    @Override
    public void cancelTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction is not found"));

        if (transaction.getStatusTransaction() != StatusTransaction.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't cancel this transaction, because it's completed");
        }

        Account sender = transaction.getAccountFrom();
        sender.setBalance(sender.getBalance().add(transaction.getAmount()));
        accountRepository.save(sender);

        transaction.setStatusTransaction(StatusTransaction.CANCELLED);
        transactionRepository.save(transaction);

    }

    public List<TransactionDTO> getTransactionDTOs(List<Transaction> transactions){
        List<TransactionDTO> transactionDTOs = new ArrayList<>();
        for(Transaction transaction : transactions){
            if(!transaction.getHidden()){
                transactionDTOs.add(transaction.toDTO());
            }
        }
        return transactionDTOs;
    }

}
