package com.example.demo.controllers;

import com.example.demo.dto.support.SupportDTO;
import com.example.demo.services.supportMessage.SupportMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/support")
public class SupportMessageController {

    private final SupportMessageService supportMessageService;

    public SupportMessageController(SupportMessageService supportMessageService) {
        this.supportMessageService = supportMessageService;
    }

    @PostMapping
    public SupportDTO createSupportMessage(@RequestBody SupportDTO supportDTO, java.security.Principal principal) {
        return supportMessageService.createSupportMessage(supportDTO, principal);
    }

}
