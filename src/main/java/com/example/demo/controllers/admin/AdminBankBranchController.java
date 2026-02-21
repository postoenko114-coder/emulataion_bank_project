package com.example.demo.controllers.admin;

import com.example.demo.dto.BankBranchDTO;
import com.example.demo.services.bankBranch.BankBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/branches")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBankBranchController {

    private final BankBranchService bankBranchService;

    public AdminBankBranchController(BankBranchService bankBranchService) {
        this.bankBranchService = bankBranchService;
    }

    @GetMapping
    public List<BankBranchDTO> getAllBankBranches() {
        return bankBranchService.getAllBankBranches();
    }

    @GetMapping("/{bankBranchId}")
    public BankBranchDTO getBankBranchById(@PathVariable Long bankBranchId) {
        return bankBranchService.getBankBranchById(bankBranchId);
    }

    @PostMapping
    public BankBranchDTO createBankBranch(@RequestBody BankBranchDTO bankBranchDTO) {
        return bankBranchService.addBankBranch(bankBranchDTO, bankBranchDTO.getLocationDTO());
    }

    @PutMapping("/{bankBranchId}")
    public BankBranchDTO editBankBranch(@RequestBody BankBranchDTO bankBranchDTO, @PathVariable Long bankBranchId) {
        return bankBranchService.updateBankBranch(bankBranchId, bankBranchDTO);
    }

    @PutMapping("/{bankBranchId}/services")
    public ResponseEntity<String> addServiceToBranch(@PathVariable Long bankBranchId, @RequestParam Long serviceId) {
        bankBranchService.addBankServiceToBranch(bankBranchId, serviceId);
        return ResponseEntity.ok("Service added to branch successfully");
    }

    @DeleteMapping("/{bankBranchId}/services")
    public ResponseEntity<String> deleteServiceFromBranch(@PathVariable Long bankBranchId, @RequestParam Long serviceId) {
        bankBranchService.deleteBankServiceFromBranch(bankBranchId, serviceId);
        return ResponseEntity.ok("Service deleted from branch successfully");
    }

    @DeleteMapping("/{bankBranchId}")
    public ResponseEntity<String> deleteBankBranch(@PathVariable Long bankBranchId) {
        bankBranchService.deleteBankBranch(bankBranchId);
        return ResponseEntity.ok("Branch deleted successfully");
    }

}
