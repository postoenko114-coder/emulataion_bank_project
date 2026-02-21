package com.example.demo.controllers;

import com.example.demo.dto.BankBranchDTO;
import com.example.demo.services.bankBranch.BankBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
public class BankBranchController {

    private final BankBranchService bankBranchService;

    public BankBranchController(BankBranchService bankBranchService) {
        this.bankBranchService = bankBranchService;
    }

    @GetMapping
    public List<BankBranchDTO> getBankBranches() {
        return bankBranchService.getAllBankBranches();
    }

    @GetMapping("/filter/service")
    public List<BankBranchDTO> getBankBranchesByService(@RequestParam String serviceName) {
        return bankBranchService.getBranchesByService(serviceName);
    }

    @GetMapping("/filter/location")
    public List<BankBranchDTO> getBankBranchesByLocation(@RequestParam(required = false) String city, @RequestParam(required = false) String address) {
        return bankBranchService.getBranchesByLocation(city, address);
    }

    @GetMapping("/filter/nearest")
    public List<BankBranchDTO> getNearestBankBranchesByLocation(@RequestParam Double latitude, @RequestParam Double longitude) {
        return bankBranchService.getNearestBranches(latitude, longitude);
    }

    @GetMapping("/{bankBranchId}")
    public BankBranchDTO getBankBranch(@PathVariable Long bankBranchId) {
        return bankBranchService.getBankBranchById(bankBranchId);
    }

}
