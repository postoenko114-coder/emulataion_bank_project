package com.example.demo.controllers.client;

import com.example.demo.dto.CardDTO;
import com.example.demo.services.account.AccountService;
import com.example.demo.services.card.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/{userId}/cards")
public class CardController {
    @Autowired
    private CardService cardService;
    @Autowired
    private AccountService accountService;

    public CardController(CardService cardService, AccountService accountService) {
        this.cardService = cardService;

        this.accountService = accountService;
    }

    @GetMapping
    public List<CardDTO> getCards(@PathVariable Long userId) {
        return cardService.getListUserCards(userId);
    }

    @GetMapping("/{cardId}")
    public CardDTO getCard(@PathVariable Long cardId) {
        return cardService.getCardById(cardId);
    }

    @PostMapping
    public CardDTO createCard(@RequestParam String accountNumber, @RequestParam String typeCard) {
        return cardService.createCard(accountService.getAccountByNumber(accountNumber).getId(), typeCard);
    }

    @PutMapping("/{cardId}/closeCard")
    public ResponseEntity<String> closeCard(@PathVariable Long cardId) {
        cardService.closeCard(cardId);
        return ResponseEntity.ok("Card Closed");
    }

}
