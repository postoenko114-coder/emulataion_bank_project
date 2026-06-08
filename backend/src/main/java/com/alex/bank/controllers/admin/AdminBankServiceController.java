package com.alex.bank.controllers.admin;

import com.alex.bank.dto.AvailabilityResponse;
import com.alex.bank.dto.BankServiceDTO;
import com.alex.bank.mapper.BankServiceMapper;
import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.services.bankBranch.BankBranchService;
import com.alex.bank.services.bankService.BankServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/services")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Services", description = "Administrative bank service management endpoints")
public class AdminBankServiceController {

    private final BankServiceService bankServiceService;

    private final BankBranchService bankBranchService;

    private final BankServiceMapper bankServiceMapper;

    @Operation(summary = "Get all bank services as admin")
    @GetMapping
    public List<BankServiceDTO> getAllBankService() {
        return bankServiceService.getServicesList();
    }

    @Operation(summary = "Get a bank service by id as admin")
    @GetMapping("/{bankServiceId}")
    public BankServiceDTO getBankServiceById(@PathVariable Long bankServiceId) {
        return bankServiceService.getServiceById(bankServiceId);
    }

    @Operation(summary = "Find bank services by name as admin")
    @GetMapping("/filter/name")
    public List<BankServiceDTO> getBankServiceByName(@RequestParam String serviceName) {
        return bankServiceService.findServiceByName(serviceName).stream()
                .map(bankServiceMapper::toDTO)
                .toList();
    }

    @Operation(summary = "Check whether a bank service is available in a branch on a date")
    @GetMapping("/{bankServiceId}/availability")
    public AvailabilityResponse getAvailabilityServiceOnDate(@PathVariable Long bankServiceId, @RequestParam String branchName, @RequestParam LocalDate date) {
        BankBranch bankBranch = bankBranchService.findBranchByName(branchName);
        if (!bankServiceService.getAvailabilityServiceByDate(bankBranch.getId(), bankServiceId, date)) {
            return new AvailabilityResponse(false, "Service not available");
        }
        return new AvailabilityResponse(true, "Service available");
    }

    @Operation(summary = "Create a new bank service as admin")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankServiceDTO addBankService(@Valid @RequestBody BankServiceDTO bankServiceDTO) {
        BankServiceDTO newBankService = bankServiceService.addService(bankServiceDTO);
        return newBankService;
    }

    @Operation(summary = "Update a bank service as admin")
    @PutMapping("/{bankServiceId}/update")
    public BankServiceDTO editService(@PathVariable Long bankServiceId, @RequestBody BankServiceDTO bankServiceDTO) {
        bankServiceService.updateService(bankServiceId, bankServiceDTO);
        return bankServiceDTO;
    }

    @Operation(summary = "Delete a bank service as admin")
    @DeleteMapping("/{bankServiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteService(@PathVariable Long bankServiceId) {
        bankServiceService.deleteService(bankServiceId);
    }

}
