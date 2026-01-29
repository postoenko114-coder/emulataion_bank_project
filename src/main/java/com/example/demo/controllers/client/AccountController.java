package com.example.demo.controllers.client;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.transaction.TransactionDTO;
import com.example.demo.services.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountDTO> getUserAccounts(@PathVariable Long userId){
        return  accountService.getListUserAccounts(userId);
    }

    @GetMapping("/{accountId}")
    public AccountDTO getUserAccount(@PathVariable Long accountId){
        return accountService.getAccountById(accountId);
    }

    @PostMapping
    public AccountDTO createUserAccount(@PathVariable Long userId, @RequestParam String currency){
        return accountService.addAccount(userId, currency);
    }

    @PostMapping("/{accountId}/withdrawal")
    public TransactionDTO makeWithdrawal(@PathVariable Long accountId, @RequestParam BigDecimal amount){
        return accountService.withdrawal(accountId, amount).toDTO();
    }

    @PostMapping("/{accountId}/transfer")
    public TransactionDTO makeTransfer(@PathVariable Long accountId, @RequestParam String accountToNumber, @RequestParam BigDecimal amount, @RequestParam String description){
        return accountService.transfer(accountId, accountService.getAccountByNumber(accountToNumber).getId(),  amount, description).toDTO();
    }

    @PostMapping("/{accountId}/deposit")
    public TransactionDTO makeDeposit(@PathVariable Long accountId, @RequestParam BigDecimal amount){
        return accountService.deposit(accountId, amount).toDTO();
    }

    @PostMapping("/{accountId}/payment")
    public TransactionDTO payByCard(@PathVariable Long accountId, @RequestParam BigDecimal amount){
        return accountService.payByCard(accountId, amount).toDTO();
    }

    @PutMapping("/{accountId}/closeAccount")
    public AccountDTO closeAccount(@PathVariable Long accountId){
        return accountService.closeAccount(accountId);
    }

}
