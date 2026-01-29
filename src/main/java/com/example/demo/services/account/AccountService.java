package com.example.demo.services.account;


import com.example.demo.dto.AccountDTO;
import com.example.demo.models.transaction.Transaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    @Transactional
    AccountDTO addAccount(Long user_id, String currencyAccount);

    @Transactional
    List<AccountDTO> getListUserAccounts(Long user_id);

    @Transactional
    AccountDTO getAccountById(Long account_id);

    @Transactional
    AccountDTO getAccountByNumber(String accountNumber);

    @Transactional
    AccountDTO blockAccount(Long account_id);

    @Transactional
    AccountDTO closeAccount(Long account_id);

    @Transactional
    AccountDTO activeAccount(Long account_id);

    @Transactional
    void removeAccount(Long account_id);

    @Transactional
    Transaction transfer(Long accountFrom_id, Long accountTo_id, BigDecimal amount, String description);

    @Transactional
    Transaction withdrawal(Long accountFrom_id, BigDecimal amount);

    @Transactional
    Transaction deposit(Long accountTo_id, BigDecimal amount);

    @Transactional
    Transaction payByCard(Long account_id, BigDecimal amount);
}
