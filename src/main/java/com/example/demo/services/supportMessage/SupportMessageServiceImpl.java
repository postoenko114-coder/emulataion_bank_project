package com.example.demo.services.supportMessage;

import com.example.demo.dto.support.SupportDTO;
import com.example.demo.models.supportMessage.StatusSupportMessage;
import com.example.demo.models.supportMessage.SupportMessage;
import com.example.demo.models.user.User;
import com.example.demo.repositories.SupportMessageRepository;
import com.example.demo.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupportMessageServiceImpl implements SupportMessageService {
    @Autowired
    private SupportMessageRepository supportMessageRepository;
    @Autowired
    private UserService userService;

    public SupportMessageServiceImpl(SupportMessageRepository supportMessageRepository, UserService userService) {
        this.supportMessageRepository = supportMessageRepository;
        this.userService = userService;
    }

    @Transactional
    @Override
    public SupportDTO createSupportMessage(SupportDTO supportDTO, java.security.Principal principal){
        SupportMessage supportMessage = new SupportMessage();
        supportMessage.setMessage(supportDTO.getMessage());
        supportMessage.setSubject(supportDTO.getSubject());
        if (principal != null) {
            User user = userService.findUserByEmail(principal.getName());

            supportMessage.setUserEmail(user.getEmail());
        } else {
            if (supportDTO.getUserEmail() == null || supportDTO.getUserEmail().isEmpty()) {
                new ResponseStatusException(HttpStatus.BAD_REQUEST,"You need to input email");
            }
            supportMessage.setUserEmail(supportDTO.getUserEmail());
        }
        supportMessage.setCreatedAt(LocalDateTime.now());
        supportMessage.setStatusSupportMessage(StatusSupportMessage.NEW);
        supportMessageRepository.save(supportMessage);
        return supportDTO;
    }

    @Transactional
    @Override
    public SupportDTO getSupportMessageById(Long supportMessage_id){
        return supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found")).toDTO();
    }

    @Transactional
    @Override
    public List<SupportDTO> getAllSupportMessages(){
        List<SupportMessage> supportMessages = supportMessageRepository.findAll();
        List<SupportDTO> supportDTOs = new ArrayList<>();
        for (SupportMessage supportMessage : supportMessages) {
            supportDTOs.add(supportMessage.toDTO());
        }
        return supportDTOs;
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

        List<SupportMessage> supportMessages = supportMessageRepository.search(userEmail, startOfDay, endOfDay);
        List<SupportDTO> supportDTOs = new ArrayList<>();
        for(SupportMessage supportMessage : supportMessages){
            supportDTOs.add(supportMessage.toDTO());
        }
        return supportDTOs;
    }

    @Transactional
    @Override
    public void markAsInProgress(Long supportMessage_id){
        SupportMessage supportMessage = supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"));
        supportMessage.setStatusSupportMessage(StatusSupportMessage.IN_PROGRESS);
    }

    @Transactional
    @Override
    public void markAsCompleted(Long supportMessage_id){
        SupportMessage supportMessage = supportMessageRepository.findSupportMessageById(supportMessage_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Support Message not found"));
        supportMessage.setStatusSupportMessage(StatusSupportMessage.COMPLETED);
    }
}
