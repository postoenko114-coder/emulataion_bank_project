package com.example.demo.services.bankService;


import com.example.demo.dto.BankServiceDTO;
import com.example.demo.models.branch.BankService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface BankServiceService {

    @Transactional
    BankServiceDTO addService(BankServiceDTO bankServiceDTO);

    @Transactional
    BankServiceDTO getServiceById(Long bankService_id);

    @Transactional
    List<BankServiceDTO> getServicesList();

    @Transactional
    List<BankService> findServiceByName(String name);

    @Transactional
    BankServiceDTO updateService(Long bankService_id, BankServiceDTO bankServiceDTO);

    @Transactional
    Boolean getAvailabilityServiceByDate(Long bankBranch_id, Long bankService_id, LocalDate date);

    @Transactional
    void  deleteService(Long bankService_id);


}
