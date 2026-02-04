package com.example.demo.services.bankService;

import com.example.demo.dto.BankServiceDTO;
import com.example.demo.models.branch.BankBranch;
import com.example.demo.models.branch.BankService;
import com.example.demo.repositories.BankBranchRepository;
import com.example.demo.repositories.BankServiceRepository;
import com.example.demo.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class BankServiceServiceImpl implements BankServiceService {
    @Autowired
    private BankServiceRepository bankServiceRepository;
    @Autowired
    private BankBranchRepository bankBranchRepository;
    @Autowired
    private ReservationRepository  reservationRepository;

    public BankServiceServiceImpl(BankServiceRepository bankServiceRepository, BankBranchRepository bankBranchRepository,  ReservationRepository reservationRepository) {
        this.bankServiceRepository = bankServiceRepository;
        this.bankBranchRepository = bankBranchRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    @Override
    public BankServiceDTO addService(BankServiceDTO bankServiceDTO){
        BankService bankService = new BankService();
        bankService.setBankServiceName(bankServiceDTO.getBankServiceName());
        bankService.setDuration(bankServiceDTO.getDuration());
        bankService.setDescription(bankServiceDTO.getDescription());
        bankServiceRepository.save(bankService);
        return bankServiceDTO;
    }

    @Transactional
    @Override
    public BankServiceDTO getServiceById(Long bankService_id){
        BankService bankService = bankServiceRepository.findById(bankService_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"));
        return bankService.toDTO();
    }

    @Transactional
    @Override
    public List<BankServiceDTO> getServicesList(){
        List<BankService> bankServiceList = bankServiceRepository.findAll();
        List<BankServiceDTO> bankServiceDTOList = new ArrayList<>();
        for(BankService bankService : bankServiceList){
            bankServiceDTOList.add(bankService.toDTO());
        }
        return bankServiceDTOList;
    }

    @Transactional
    @Override
    public List<BankService> findServiceByName(String name) {
        List<BankService> bankServices = bankServiceRepository.findByBankServiceName(name);
        return bankServices;
    }

    @Transactional
    @Override
    public BankServiceDTO updateService(Long bankService_id, BankServiceDTO bankServiceDTO){
        BankService bankService = bankServiceRepository.findById(bankService_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"));
        if(!bankService.getBankServiceName().equals(bankServiceDTO.getBankServiceName())){
            bankService.setBankServiceName(bankServiceDTO.getBankServiceName());
        }
        if(!bankService.getDuration().equals(bankServiceDTO.getDuration())){
            bankService.setDuration(bankServiceDTO.getDuration());
        }
        if(!bankService.getDescription().equals(bankServiceDTO.getDescription())){
            bankService.setDescription(bankServiceDTO.getDescription());
        }
        return bankServiceDTO;
    }

    @Transactional
    @Override
    public Boolean getAvailabilityServiceByDate(Long bankBranch_id, Long bankService_id, LocalDate date) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }

        BankBranch bankBranch = bankBranchRepository.findById(bankBranch_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Branch Not Found"));

        BankService bankService = bankServiceRepository.findById(bankService_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"));

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isOpenToday = bankBranch.getSchedule().stream()
                .anyMatch(s -> s.getDayOfWeek().toString().equalsIgnoreCase(dayOfWeek.toString()));

        if (!isOpenToday) {
            return false;
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return reservationRepository.countBookedSlots(bankBranch_id, bankService_id, startOfDay, endOfDay) < 10;
    }

    @Transactional
    @Override
    public void deleteService(Long bankService_id){
        bankServiceRepository.deleteById(bankService_id);
    }

}
