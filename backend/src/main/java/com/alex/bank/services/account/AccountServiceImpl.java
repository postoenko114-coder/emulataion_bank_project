package com.alex.bank.services.account;

import com.alex.bank.dto.AccountDTO;
import com.alex.bank.dto.transaction.TransactionDTO;
import com.alex.bank.mapper.AccountMapper;
import com.alex.bank.mapper.TransactionMapper;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.account.CurrencyAccount;
import com.alex.bank.models.account.StatusAccount;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.AccountRepository;
import com.alex.bank.repositories.UserRepository;
import com.alex.bank.services.notification.NotificationService;
import com.alex.bank.services.transaction.TransactionService;
import com.alex.bank.utils.currencyConvert.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    private final TransactionService transactionService;

    private final NotificationService notificationService;

    private final AccountMapper accountMapper;

    private final TransactionMapper transactionMapper;

    private final Converter converter;

    @Transactional
    @Override
    public AccountDTO addAccount(Long user_id, String currencyAccount) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setCurrencyAccount(parseCurrency(currencyAccount));
        account.setUser(user);
        account.setBalance(new BigDecimal(0));
        account.setCreatedAt(LocalDateTime.now());
        account.setStatusAccount(StatusAccount.ACTIVE);
        accountRepository.save(account);
        user.getAccounts().add(account);
        log.info("Account opened accountId={} userId={} currency={} accountNumber={}",
                account.getId(), user.getId(), account.getCurrencyAccount(), maskNumber(account.getAccountNumber()));
        return accountMapper.toDTO(account);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccountDTO> getListUserAccounts(Long user_id) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getAccounts().stream()
                .map(accountMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public AccountDTO getAccountById(Long account_id) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        return accountMapper.toDTO(account);
    }

    @Transactional(readOnly = true)
    @Override
    public AccountDTO getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return accountMapper.toDTO(account);
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
        log.info("Account blocked accountId={} userId={}", account.getId(), account.getUser().getId());
        return accountMapper.toDTO(account);
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
        log.info("Account closed accountId={} userId={}", account.getId(), account.getUser().getId());
        return accountMapper.toDTO(account);
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
        log.info("Account activated accountId={} userId={}", account.getId(), account.getUser().getId());
        return accountMapper.toDTO(account);
    }

    @Transactional
    @Override
    public void removeAccount(Long account_id) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Account not found"));
        accountRepository.delete(account);
        log.info("Account deleted accountId={}", account_id);
    }

    @Transactional
    @Override
    public TransactionDTO transfer(Long accountFrom_id, Long accountTo_id, BigDecimal amount, String description) {
        Account accountFrom = accountRepository.findById(accountFrom_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        Account accountTo = accountRepository.findById(accountTo_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!validateAccount(accountFrom)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (!validateAccount(accountTo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (accountFrom.getBalance().compareTo(amount) < 0) {
            log.warn("Transfer rejected due to insufficient funds accountFromId={} accountToId={} amount={}", accountFrom_id, accountTo_id, amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Transfer rejected due to non-positive amount accountFromId={} accountToId={} amount={}", accountFrom_id, accountTo_id, amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
        if (!accountTo.getCurrencyAccount().equals(accountFrom.getCurrencyAccount())) {
            BigDecimal convertedAmount = converter.getConvertToCurrency(accountFrom.getCurrencyAccount(), accountTo.getCurrencyAccount(), amount);
            accountTo.setBalance(accountTo.getBalance().add(convertedAmount));
        } else {
            accountTo.setBalance(accountTo.getBalance().add(amount));
        }
        notificationService.notifyTransfer(accountFrom, accountTo, amount);
        TransactionDTO transaction = transactionService.recordTransfer(accountTo, accountFrom, amount, description);
        log.info("Transfer completed transactionId={} accountFromId={} accountToId={} amount={} currencyFrom={} currencyTo={}",
                transaction.getId(), accountFrom.getId(), accountTo.getId(), amount, accountFrom.getCurrencyAccount(), accountTo.getCurrencyAccount());
        return transaction;
    }

    @Transactional
    @Override
    public TransactionDTO withdrawal(Long accountFrom_id, BigDecimal amount) {
        Account account = accountRepository.findById(accountFrom_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!validateAccount(account)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Withdrawal rejected due to insufficient funds accountId={} amount={}", accountFrom_id, amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Withdrawal rejected due to non-positive amount accountId={} amount={}", accountFrom_id, amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        account.setBalance(account.getBalance().subtract(amount));
        notificationService.notifyWithdrawal(account, amount);
        TransactionDTO transaction = transactionService.recordWithdrawal(account, amount);
        log.info("Withdrawal completed transactionId={} accountId={} amount={} currency={}",
                transaction.getId(), account.getId(), amount, account.getCurrencyAccount());
        return transaction;
    }

    @Transactional
    @Override
    public TransactionDTO deposit(Long accountTo_id, BigDecimal amount) {
        Account account = accountRepository.findById(accountTo_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!validateAccount(account)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Deposit rejected due to non-positive amount accountId={} amount={}", accountTo_id, amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        account.setBalance(account.getBalance().add(amount));
        notificationService.notifyDeposit(account, amount);
        TransactionDTO transaction = transactionService.recordDeposit(account, amount);
        log.info("Deposit completed transactionId={} accountId={} amount={} currency={}",
                transaction.getId(), account.getId(), amount, account.getCurrencyAccount());
        return transaction;
    }

    @Transactional
    @Override
    public TransactionDTO payByCard(Long account_id, BigDecimal amount) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!validateAccount(account)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is not active");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Card payment rejected due to insufficient funds accountId={} amount={}", account_id, amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Card payment rejected due to non-positive amount accountId={} amount={}", account_id, amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        account.setBalance(account.getBalance().subtract(amount));
        notificationService.notifyPaymentByCard(account, amount);
        TransactionDTO transaction = transactionService.recordPaymentByCard(account, amount);
        log.info("Card payment completed transactionId={} accountId={} amount={} currency={}",
                transaction.getId(), account.getId(), amount, account.getCurrencyAccount());
        return transaction;
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

    private boolean validateAccount(Account account) {
        if (account.getStatusAccount().equals(StatusAccount.ACTIVE)) {
            return true;
        } else {
            return false;
        }
    }

    private String maskNumber(String number) {
        if (number == null || number.length() <= 4) {
            return "****";
        }
        return "****" + number.substring(number.length() - 4);
    }

    private CurrencyAccount parseCurrency(String currencyAccount) {
        try {
            return CurrencyAccount.valueOf(currencyAccount.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account currency");
        }
    }

}
