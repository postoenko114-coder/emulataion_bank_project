package com.example.demo.controllers;

import com.example.demo.dto.BankServiceDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class BankServiceController {

    private final BankServiceService bankServiceService;

    private final BankBranchService bankBranchService;

    public BankServiceController(BankServiceService bankServiceService, BankBranchService bankBranchService) {
        this.bankServiceService = bankServiceService;
        this.bankBranchService = bankBranchService;
    }

    @GetMapping("/{bankServiceId}")
    public BankServiceDTO getBankService(@PathVariable Long bankServiceId) {
        return bankServiceService.getServiceById(bankServiceId);
    }

    @GetMapping("/filter/name")
    public List<BankServiceDTO> getBankServiceByName(@RequestParam String name) {
        List<BankService> bankServices = bankServiceService.findServiceByName(name);
        List<BankServiceDTO> bankServiceDTOs = new ArrayList<>();
        for (BankService bankService : bankServices) {
            bankServiceDTOs.add(bankService.toDTO());
        }
        return bankServiceDTOs;
    }

    @GetMapping
    public List<BankServiceDTO> getAllBankService() {
        return bankServiceService.getServicesList();
    }

}
