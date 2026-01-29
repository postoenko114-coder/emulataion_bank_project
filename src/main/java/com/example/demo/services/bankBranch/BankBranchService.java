package com.example.demo.services.bankBranch;

import com.example.demo.dto.*;
import com.example.demo.models.branch.BankBranch;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface BankBranchService {
    @Transactional
    BankBranchDTO addBankBranch(BankBranchDTO bankBranchDTO, LocationDTO locationDTO);

    @Transactional
    BankBranchDTO updateBankBranch(Long bankBranch_id, BankBranchDTO bankBranchDTO);

    @Transactional
    void deleteBankBranch(Long bankBranch_id);

    @Transactional
    BankBranchDTO getBankBranchById(Long bankBranch_id);

    @Transactional
    List<BankBranchDTO> getAllBankBranches();

    @Transactional
    List<BankBranchDTO> getBranchesByService(String serviceName);

    @Transactional
    List<BankBranchDTO> getBranchesByLocation(String city, String street);

    @Transactional
    List<BankBranchDTO> getNearestBranches(Double userLat, Double userLot);

    @Transactional
    void addBankServiceToBranch(Long bankBranch_id, Long bankService_id);

    @Transactional
    void deleteBankServiceFromBranch(Long bankBranch_id, Long bankService_id);

    @Transactional
    List<BankServiceDTO> getBankServicesOfBranch(Long bankBranch_id);

    @Transactional
    boolean isBranchOpen(Long bankBranch_id, LocalDateTime dateTime);

    @Transactional
    BankBranch findBranchByName(String branchName);
}
