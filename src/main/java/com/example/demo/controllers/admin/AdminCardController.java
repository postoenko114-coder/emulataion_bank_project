package com.example.demo.controllers.admin;

import com.example.demo.dto.CardDTO;
import com.example.demo.services.account.AccountService;
import com.example.demo.services.card.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/{userId}/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {
    @Autowired
    CardService cardService;
    @Autowired
    AccountService accountService;

    public AdminCardController(CardService cardService, AccountService accountService) {
        this.cardService = cardService;
        this.accountService = accountService;
    }

    @GetMapping
    public List<CardDTO> getUserCards(@PathVariable Long userId) {
        return  cardService.getListUserCards(userId);
    }

    @GetMapping ("/{cardId}")
    public CardDTO getUserCard(@PathVariable Long cardId) {
        return cardService.getCardById(cardId);
    }

    @GetMapping("/filter/number")
    public CardDTO getUserCardByNumber(@RequestParam String cardNumber) {
        return cardService.findCardByNumber(cardNumber);
    }

    @PostMapping
    public CardDTO createUserCard(@RequestParam String accountNumber, @RequestParam String typeCard) {
        return cardService.createCard(accountService.getAccountByNumber(accountNumber).getId(), typeCard);
    }

    @PutMapping("/{cardId}/blockCard")
    public ResponseEntity<String> blockUserCard(@PathVariable Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok("Card has been blocked");
    }

    @PutMapping("/{cardId}/activateCard")
    public ResponseEntity<String> activateUserCard(@PathVariable Long cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok("Card has been unblocked");
    }

    @PutMapping("/{cardId}/changeTypeCard")
    public ResponseEntity<String> changeTypeCard(@PathVariable Long cardId) {
        cardService.changeTypeCard(cardId);
        return ResponseEntity.ok("Type Card has been changed");
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<String> deleteUserCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Card has been deleted");
    }

}
