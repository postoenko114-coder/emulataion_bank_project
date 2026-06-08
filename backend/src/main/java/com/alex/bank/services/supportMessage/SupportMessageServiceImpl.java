package com.alex.bank.services.supportMessage;

import com.alex.bank.dto.support.SupportDTO;
import com.alex.bank.mapper.SupportMapper;
import com.alex.bank.models.supportMessage.StatusSupportMessage;
import com.alex.bank.models.supportMessage.SupportMessage;
import com.alex.bank.repositories.SupportMessageRepository;
import com.alex.bank.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SupportMessageServiceImpl implements SupportMessageService {

    private final SupportMessageRepository supportMessageRepository;

    private final UserService userService;

    private final SupportMapper  supportMapper;

    @Transactional
    @Override
    public SupportDTO createSupportMessage(SupportDTO supportDTO, java.security.Principal principal) {
        String targetEmail;
        if (principal != null) {
            targetEmail = principal.getName();
        } else {
            targetEmail = supportDTO.getUserEmail();
            if (targetEmail == null || targetEmail.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to input email");
            }
        }

        SupportMessage supportMessage = supportMapper.toEntity(supportDTO);

        supportMessage.setUserEmail(targetEmail);

        supportMessageRepository.save(supportMessage);
        log.info("Support message created supportMessageId={} email={}", supportMessage.getId(), maskEmail(targetEmail));
        return supportMapper.toDTO(supportMessage);
    }

    @Transactional
    @Override
    public SupportDTO getSupportMessageById(Long supportMessage_id){
        SupportMessage message = supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"));
        return supportMapper.toDTO(message);
    }

    @Transactional
    @Override
    public List<SupportDTO> getAllSupportMessages(){
        return supportMessageRepository.findAll().stream()
                .map(supportMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public List<SupportDTO> search(String userEmail, LocalDate date){
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;

        if (date != null) {
            startOfDay = date.atStartOfDay();
            endOfDay = date.atTime(LocalTime.MAX);
        }

        return supportMessageRepository.search(userEmail, startOfDay, endOfDay).stream()
                .map(supportMapper::toDTO)
                .toList();
    }

    @Transactional
    @Override
    public void markAsInProgress(Long supportMessage_id){
        SupportMessage supportMessage = supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"));
        supportMessage.setStatusSupportMessage(StatusSupportMessage.IN_PROGRESS);
        supportMessageRepository.save(supportMessage);
        log.info("Support message marked in progress supportMessageId={}", supportMessage.getId());
    }

    @Transactional
    @Override
    public void markAsCompleted(Long supportMessage_id){
        SupportMessage supportMessage = supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"));
        supportMessage.setStatusSupportMessage(StatusSupportMessage.COMPLETED);
        supportMessageRepository.save(supportMessage);
        log.info("Support message completed supportMessageId={}", supportMessage.getId());
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String visible = name.isEmpty() ? "*" : name.substring(0, Math.min(2, name.length()));
        return visible + "***@" + parts[1];
    }
}
