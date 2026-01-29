package com.example.demo.services.account;

import com.example.demo.dto.AccountDTO;
import com.example.demo.models.account.Account;
import com.example.demo.models.account.CurrencyAccount;
import com.example.demo.models.account.StatusAccount;
import com.example.demo.models.transaction.Transaction;
import com.example.demo.models.user.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.notification.NotificationService;
import com.example.demo.services.transaction.TransactionService;
import com.example.demo.utils.currencyConvert.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private NotificationService notificationService;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository, TransactionService transactionService, NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
    }

    @Transactional
    @Override
    public AccountDTO addAccount(Long user_id, String currencyAccount) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setCurrencyAccount(CurrencyAccount.valueOf(currencyAccount.toUpperCase()));
        account.setUser(user);
        account.setBalance(new BigDecimal(0));
        account.setCreatedAt(LocalDateTime.now());
        account.setStatusAccount(StatusAccount.ACTIVE);
        accountRepository.save(account);
        user.getAccounts().add(account);
        return account.toDTO();
    }

    @Transactional
    @Override
    public List<AccountDTO> getListUserAccounts(Long user_id) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Account> accounts = user.getAccounts();
        List<AccountDTO> dtos = new ArrayList<>();
        for (Account account : accounts) {
            dtos.add(account.toDTO());
        }
        return dtos;
    }

    @Transactional
    @Override
    public AccountDTO getAccountById(Long account_id) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        return account.toDTO();
    }

    @Transactional
    @Override
    public AccountDTO getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return account.toDTO();
    }

    @Transactional
    @Override
    public AccountDTO blockAccount(Long account_id) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getStatusAccount().equals(StatusAccount.BLOCKED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is already blocked");
        } else {
            account.setStatusAccount(StatusAccount.BLOCKED);
        }
        notificationService.notifyPersonalMessage(account.getUser().getId(), "Your account: " + account.getAccountNumber() + " has been blocked");
        return account.toDTO();
    }

    @Transactional
    @Override
    public AccountDTO closeAccount(Long account_id) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getStatusAccount().equals(StatusAccount.CLOSED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is already closed");
        } else {
            account.setStatusAccount(StatusAccount.CLOSED);
        }
        notificationService.notifyPersonalMessage(account.getUser().getId(), "Your account: " + account.getAccountNumber() + " has been closed");
        return account.toDTO();
    }

    @Transactional
    @Override
    public AccountDTO activeAccount(Long account_id) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getStatusAccount().equals(StatusAccount.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is already active");
        } else {
            account.setStatusAccount(StatusAccount.ACTIVE);
        }
        notificationService.notifyPersonalMessage(account.getUser().getId(), "Your account: " + account.getAccountNumber() + " has been activated");
        return account.toDTO();
    }

    @Transactional
    @Override
    public void removeAccount(Long account_id) {
        accountRepository.deleteById(account_id);
    }

    @Transactional
    @Override
    public Transaction transfer(Long accountFrom_id, Long accountTo_id, BigDecimal amount, String description) {
        Converter converter = new Converter();
        Account accountFrom = accountRepository.findById(accountFrom_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        Account accountTo = accountRepository.findById(accountTo_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!validateAccount(accountFrom_id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (!validateAccount(accountTo_id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (accountFrom.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        if (!accountTo.getCurrencyAccount().equals(accountFrom.getCurrencyAccount())) {
            accountTo.setBalance(converter.getConvertToCurrency(accountFrom.getCurrencyAccount(), accountTo.getCurrencyAccount(), amount));
        }else{
            accountTo.setBalance(accountTo.getBalance().add(amount));
        }
        notificationService.notifyTransfer(accountFrom, accountTo, amount);
        return transactionService.recordTransfer(accountFrom, accountTo, amount, description);
    }

    @Transactional
    @Override
    public Transaction withdrawal(Long accountFrom_id, BigDecimal amount) {
        Account account = accountRepository.findById(accountFrom_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!validateAccount(accountFrom_id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        account.setBalance(account.getBalance().subtract(amount));
        notificationService.notifyWithdrawal(account, amount);
        return transactionService.recordWithdrawal(account, amount);
    }

    @Transactional
    @Override
    public Transaction deposit(Long accountTo_id, BigDecimal amount) {
        Account account = accountRepository.findById(accountTo_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!validateAccount(accountTo_id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        account.setBalance(account.getBalance().add(amount));
        notificationService.notifyDeposit(account, amount);
        return transactionService.recordDeposit(account, amount);
    }

    @Transactional
    @Override
    public Transaction payByCard(Long account_id, BigDecimal amount) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!validateAccount(account_id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
        notificationService.notifyPaymentByCard(account, amount);
        return transactionService.recordPaymentByCard(account, amount);
    }

    private String generateAccountNumber() {
        String accountNumber;
        boolean exists;
        Random random = new Random();

        do {
            String left = String.format("%09d", random.nextInt(1_000_000_000));
            String right = String.format("%04d", random.nextInt(10_000));
            accountNumber = left + "/" + right;

            exists = accountRepository.existsByAccountNumber(accountNumber);

        } while (exists);

        return accountNumber;
    }

    private boolean validateAccount(Long account_id) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getStatusAccount().equals(StatusAccount.ACTIVE)) {
            return true;
        } else {
            return false;
        }
    }

}
