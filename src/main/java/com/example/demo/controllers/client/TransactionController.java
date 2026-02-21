package com.example.demo.controllers.client;

import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.services.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionDTO> getUserTransactions(@PathVariable Long userId, @RequestParam(required = false, defaultValue = "0") Integer page) {
        List<Transaction> transactions = transactionService.getListUserTransaction(userId, PageRequest.of(page, 10));
        return getTransactionDTOs(transactions);
    }

    @GetMapping("/filter/{accountId}")
    public List<TransactionDTO> getAccountTransactions(@PathVariable Long accountId, @RequestParam(required = false, defaultValue = "0") Integer page) {
        List <Transaction> transactions = transactionService.getListAccountTransactions(accountId, PageRequest.of(page, 10));
        return getTransactionDTOs(transactions);
    }

    @GetMapping("/{transactionId}")
    public TransactionDTO getTransaction(@PathVariable Long transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        if(transaction.getHidden()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction is hidden");
        }
        return transaction.toDTO();
    }

    @GetMapping("/filter/date")
    public List<TransactionDTO> getTransactionsByDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @RequestParam(required = false, defaultValue = "0") Integer page) {
        List<Transaction> transactions = transactionService.getListAccountTransactionsInDate(userId, date,  PageRequest.of(page, 10));
        return getTransactionDTOs(transactions);
    }

    @GetMapping("/filter/afterDate")
    public List<TransactionDTO> getTransactionAfterDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @RequestParam(required = false, defaultValue = "0") Integer page) {
        List<Transaction> transactions = transactionService.getListAccountTransactionsAfterDate(userId, date,   PageRequest.of(page, 10));
        return getTransactionDTOs(transactions);
    }

    @GetMapping("/filter/beforeDate")
    public List<TransactionDTO> getTransactionBeforeDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @RequestParam(required = false, defaultValue = "0") Integer page) {
        List <Transaction> transactions = transactionService.getListAccountTransactionsBeforeDate(userId, date ,  PageRequest.of(page, 10));
       return getTransactionDTOs(transactions);
    }

    @GetMapping("/filter/amount")
    public List<TransactionDTO> getTransactionsByAmount(@PathVariable Long userId, @RequestParam BigDecimal amount, @RequestParam(required = false, defaultValue = "0") Integer page) {
        List<Transaction> transactions = transactionService.getListAccountTransactionsByAmount(userId, amount,   PageRequest.of(page, 10));
        return getTransactionDTOs(transactions);
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<String> hideTransaction(@PathVariable Long transactionId) {
        transactionService.hideTransaction(transactionId);
        return  ResponseEntity.ok("Transaction has been hidden");
    }

    private List<TransactionDTO> getTransactionDTOs(List<Transaction> transactions){
        List<TransactionDTO> transactionDTOs = new ArrayList<>();
        for(Transaction transaction : transactions){
            if(!transaction.getHidden()){
                transactionDTOs.add(transaction.toDTO());
            }
        }
        return transactionDTOs;
    }

}
