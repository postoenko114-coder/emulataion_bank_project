package com.alex.bank.controllers;

import com.alex.bank.dto.BankServiceDTO;
import com.alex.bank.mapper.BankServiceMapper;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.services.bankService.BankServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Public Services", description = "Public bank service catalog endpoints")
public class BankServiceController {

    private final BankServiceService bankServiceService;

    private final BankServiceMapper bankServiceMapper;

    @Operation(summary = "Get a public bank service by id")
    @GetMapping("/{bankServiceId}")
    public BankServiceDTO getBankService(@PathVariable Long bankServiceId) {
        return bankServiceService.getServiceById(bankServiceId);
    }

    @Operation(summary = "Find public bank services by name")
    @GetMapping("/filter/name")
    public List<BankServiceDTO> getBankServiceByName(@RequestParam String name) {
        return bankServiceService.findServiceByName(name).stream()
                .map(bankServiceMapper::toDTO)
                .toList();
    }

    @Operation(summary = "Get all public bank services")
    @GetMapping
    public List<BankServiceDTO> getAllBankService() {
        return bankServiceService.getServicesList();
    }

}
