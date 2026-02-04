package com.example.demo.repositories;

import com.example.demo.models.branch.BankService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BankServiceRepository extends JpaRepository<BankService,Long> {

    @Query("SELECT ss FROM BankService ss WHERE UPPER(ss.bankServiceName) LIKE UPPER(CONCAT('%', :serviceName, '%'))")
    List<BankService> findByBankServiceName(@Param("serviceName") String serviceName);
}
