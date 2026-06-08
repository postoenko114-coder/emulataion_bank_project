package com.alex.bank.services.bankService;

import com.alex.bank.dto.BankServiceDTO;
import com.alex.bank.mapper.BankServiceMapper;
import com.alex.bank.models.branch.BankBranch;
import com.alex.bank.models.branch.BankService;
import com.alex.bank.repositories.BankBranchRepository;
import com.alex.bank.repositories.BankServiceRepository;
import com.alex.bank.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankServiceServiceImpl implements BankServiceService {

    private final BankServiceRepository bankServiceRepository;

    private final BankBranchRepository bankBranchRepository;

    private final ReservationRepository  reservationRepository;

    private final BankServiceMapper bankServiceMapper;

    @Transactional
    @Override
    public BankServiceDTO addService(BankServiceDTO bankServiceDTO){
        BankService bankService = bankServiceMapper.toEntity(bankServiceDTO);
        BankService saved = bankServiceRepository.save(bankService);
        log.info("Bank service created serviceId={} name={}", saved.getId(), saved.getBankServiceName());
        return bankServiceMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public BankServiceDTO getServiceById(Long bankService_id){
        BankService bankService = bankServiceRepository.findById(bankService_id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"));
        return bankServiceMapper.toDTO(bankService);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankServiceDTO> getServicesList(){
        return bankServiceRepository.findAll().stream()
                .map(bankServiceMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
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
        BankService saved = bankServiceRepository.save(bankService);
        log.info("Bank service updated serviceId={} name={}", saved.getId(), saved.getBankServiceName());
        return bankServiceMapper.toDTO(saved);
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
                .anyMatch(s -> s.getDay().toString().equalsIgnoreCase(dayOfWeek.toString()));

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
        BankService bankService = bankServiceRepository.findById(bankService_id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank Service Not Found"));
        bankServiceRepository.delete(bankService);
        log.info("Bank service deleted serviceId={}", bankService_id);
    }

}
