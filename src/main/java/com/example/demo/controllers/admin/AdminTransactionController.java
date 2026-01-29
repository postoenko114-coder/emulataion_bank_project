package com.example.demo.controllers.admin;

import com.example.demo.dto.transaction.TransactionDTOAdmin;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.services.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/transactions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTransactionController {
    @Autowired
    private TransactionService transactionService;

    public AdminTransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionDTOAdmin> getUserTransactions(@PathVariable Long userId, @RequestParam(required = false, defaultValue = "0") Integer page){
        return getTransactionDTOAdmins(transactionService.getListUserTransactionForAdmin(userId, PageRequest.of(page, 10)));
    }

    @GetMapping("/filter/{accountId}")
    public List<TransactionDTOAdmin> getAccountTransactions(@PathVariable Long accountId, @RequestParam(required = false, defaultValue = "0") Integer page){
        return getTransactionDTOAdmins(transactionService.getListAccountTransactions(accountId,  PageRequest.of(page, 10)));
    }

    @GetMapping("/{transactionId}")
    public TransactionDTOAdmin getTransaction(@PathVariable Long transactionId) {
        return transactionService.getTransactionById(transactionId).toDTOAdmin();
    }

    @GetMapping("/filter/date")
    public List<TransactionDTOAdmin> getTransactionsByDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @RequestParam(required = false, defaultValue = "0") Integer page) {
        return getTransactionDTOAdmins(transactionService.getListAccountTransactionsInDate(userId, date,  PageRequest.of(page, 10)));
    }

    @GetMapping("/filter/amount")
    public List<TransactionDTOAdmin> getTransactionsByAmount(@PathVariable Long userId,  @RequestParam BigDecimal amount ,@RequestParam(required = false, defaultValue = "0") Integer page) {
        return getTransactionDTOAdmins(transactionService.getListAccountTransactionsByAmount(userId, amount,   PageRequest.of(page, 10)));
    }

    @GetMapping("/filter/beforeDate")
    public List<TransactionDTOAdmin> getTransactionsBeforeDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @RequestParam(required = false, defaultValue = "0") Integer page) {
        return getTransactionDTOAdmins(transactionService.getListAccountTransactionsBeforeDate(userId, date, PageRequest.of(page, 10)));
    }

    @GetMapping("/filter/afterDate")
    public List<TransactionDTOAdmin> getTransactionsAfterDate(@PathVariable Long userId, @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date, @RequestParam(required = false, defaultValue = "0") Integer page) {
        return getTransactionDTOAdmins(transactionService.getListAccountTransactionsAfterDate(userId, date, PageRequest.of(page, 10)));
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<String> cancelTransaction(@PathVariable Long transactionId) {
        transactionService.cancelTransaction(transactionId);
        return ResponseEntity.ok("Transaction cancelled");
    }

    private List<TransactionDTOAdmin> getTransactionDTOAdmins(List<Transaction> transactions){
        List<TransactionDTOAdmin> transactionDTOAdmins = new ArrayList<>();
        for(Transaction transaction : transactions){
            transactionDTOAdmins.add(transaction.toDTOAdmin());
        }
        return transactionDTOAdmins;
    }
}
