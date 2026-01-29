package com.example.demo.controllers.admin;

import com.example.demo.dto.AccountDTO;
import com.example.demo.services.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/accounts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {
    @Autowired
    private AccountService accountService;

    public AdminAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountDTO> getUserAccounts(@PathVariable Long userId) {
        return accountService.getListUserAccounts(userId);
    }

    @GetMapping("/filter/number")
    public AccountDTO findByNumber(@RequestParam String accountNumber) {
        return accountService.getAccountByNumber(accountNumber);
    }

    @GetMapping("/{accountId}")
    public AccountDTO getAccount(@PathVariable Long accountId) {
        return accountService.getAccountById(accountId);
    }

    @PostMapping
    public AccountDTO createAccount(@PathVariable Long userId, @RequestParam String currency) {
        return accountService.addAccount(userId, currency);
    }

    @PutMapping("/{accountId}/closeAccount")
    public AccountDTO closeAccount(@PathVariable Long accountId) {
        return accountService.closeAccount(accountId);
    }

    @PutMapping("/{accountId}/blockAccount")
    public AccountDTO blockAccount(@PathVariable Long accountId) {
        return accountService.blockAccount(accountId);
    }

    @PutMapping("/{accountId}/activateAccount")
    public AccountDTO activateAccount(@PathVariable Long accountId) {
       return accountService.activeAccount(accountId);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long accountId) {
        accountService.removeAccount(accountId);
        return  ResponseEntity.ok("Account deleted");
    }

}
