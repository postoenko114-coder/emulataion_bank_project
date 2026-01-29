package com.example.demo.services.supportMessage;

import com.example.demo.dto.support.SupportDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface SupportMessageService {

    @Transactional
    SupportDTO createSupportMessage(SupportDTO supportDTO, java.security.Principal principal);

    @Transactional
    SupportDTO getSupportMessageById(Long supportMessage_id);

    @Transactional
    List<SupportDTO> getAllSupportMessages();

    @Transactional
    List<SupportDTO> search(String userEmail, LocalDate date);

    @Transactional
    void markAsInProgress(Long supportMessage_id);

    @Transactional
    void markAsCompleted(Long supportMessage_id);
}
