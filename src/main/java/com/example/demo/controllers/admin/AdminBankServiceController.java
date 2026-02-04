package com.example.demo.controllers.admin;

import com.example.demo.dto.BankServiceDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.services.bankBranch.BankBranchService;
import com.example.demo.services.bankService.BankServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBankServiceController {
    @Autowired
    private BankServiceService bankServiceService;
    @Autowired
    private BankBranchService bankBranchService;

    public AdminBankServiceController(BankServiceService bankServiceService, BankBranchService bankBranchService) {
        this.bankServiceService = bankServiceService;
        this.bankBranchService = bankBranchService;
    }

    @GetMapping
    public List<BankServiceDTO> getAllBankService() {
        return bankServiceService.getServicesList();
    }

    @GetMapping("/{bankServiceId}")
    public BankServiceDTO getBankServiceById(@PathVariable Long bankServiceId) {
        return bankServiceService.getServiceById(bankServiceId);
    }

    @GetMapping("/filter/name")
    public List<BankServiceDTO> getBankServiceByName(@RequestParam String serviceName) {
        List<BankService> bankServices = bankServiceService.findServiceByName(serviceName);
        List<BankServiceDTO> bankServiceDTOs = new ArrayList<>();
        for (BankService bankService : bankServices) {
            bankServiceDTOs.add(bankService.toDTO());
        }
        return bankServiceDTOs;
    }

    @GetMapping("/{bankServiceId}/availability")
    public ResponseEntity<String> getAvailabilityServiceOnDate(@PathVariable Long bankServiceId, @RequestParam String branchName, @RequestParam LocalDate date) {
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        if (!bankServiceService.getAvailabilityServiceByDate(bankBranch.getId(), bankServiceId, date)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok("Service available on date " + date.toString());
    }

    @PostMapping
    public ResponseEntity<String> addBankService(@RequestBody BankServiceDTO bankServiceDTO) {
        bankServiceService.addService(bankServiceDTO);
        return ResponseEntity.ok("Bank service was created successfully");
    }

    @PutMapping("/{bankServiceId}/update")
    public ResponseEntity<String> editService(@PathVariable Long bankServiceId, @RequestBody BankServiceDTO bankServiceDTO) {
        bankServiceService.updateService(bankServiceId, bankServiceDTO);
        return ResponseEntity.ok("Changes was saved successfully");
    }

    @DeleteMapping("/{bankServiceId}")
    public ResponseEntity<String> deleteService(@PathVariable Long bankServiceId) {
        bankServiceService.deleteService(bankServiceId);
        return ResponseEntity.ok("Service was deleted successfully");
    }

}
