package com.example.demo.services.card;

import com.example.demo.dto.CardDTO;
import com.example.demo.models.account.Account;
import com.example.demo.models.account.StatusAccount;
import com.example.demo.models.card.Card;
import com.example.demo.models.card.StatusCard;
import com.example.demo.models.card.TypeCard;
import com.example.demo.models.user.User;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.CardRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.account.AccountService;
import com.example.demo.services.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class CardServiceImpl implements CardService {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;

    public CardServiceImpl(AccountService accountService, AccountRepository accountRepository, CardRepository cardRepository, UserRepository userRepository, NotificationService notificationService) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    @Override
    public CardDTO createCard(Long account_id, String typeCard) {
        Account account = accountRepository.findById(account_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (account.getCard() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already exists for this account");
        }
        Card card = new Card();
        card.setAccount(account);
        card.setCardNumber(createRandomCardNumber());
        card.setCardHolderName(account.getUser().getRealUsername());
        card.setStatusCard(StatusCard.ACTIVE);
        card.setTypeCard(TypeCard.valueOf(typeCard.toUpperCase()));
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setUser(account.getUser());
        account.setCard(card);
        cardRepository.save(card);
        return card.toDTO();
    }

    @Transactional
    @Override
    public List<CardDTO> getListUserCards(Long user_id) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Card> cards = user.getCards();
        List<CardDTO> cardDTOs = new ArrayList<>();
        for (Card card : cards) {
            cardDTOs.add(card.toDTO());
        }
        return cardDTOs;
    }

    @Transactional
    @Override
    public CardDTO getCardById(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        return card.toDTO();
    }

    @Transactional
    @Override
    public CardDTO findCardByNumber(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        return card.toDTO();
    }

    @Transactional
    @Override
    public void blockCard(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (card.getStatusCard() == StatusCard.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already blocked");
        } else {
            card.setStatusCard(StatusCard.BLOCKED);
            notificationService.notifyPersonalMessage(card.getUser().getId(), "Your card " + card.getCardNumber() + " has been blocked");
        }
    }

    @Transactional
    @Override
    public void closeCard(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if(card.getStatusCard() == StatusCard.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already closed");
        } else{
            card.setStatusCard(StatusCard.CLOSED);
            notificationService.notifyPersonalMessage(card.getUser().getId(), "Your card " + card.getCardNumber() + " has been closed");
        }
    }

    @Transactional
    @Override
    public void activateCard(Long card_id){
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if(card.getStatusCard() == StatusCard.ACTIVE){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already active");
        } else {
            card.setStatusCard(StatusCard.ACTIVE);
            notificationService.notifyPersonalMessage(card.getUser().getId(), "Your card " + card.getCardNumber() + " has been activated");
        }
    }

    @Transactional
    @Override
    public void changeTypeCard(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (card.getTypeCard() == TypeCard.DEBIT) {
            card.setTypeCard(TypeCard.CREDIT);
        } else if (card.getTypeCard() == TypeCard.CREDIT) {
            card.setTypeCard(TypeCard.DEBIT);
        }
    }

    @Transactional
    @Override
    public void payByCard(Long card_id, BigDecimal amount) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        validateCard(card);

        accountService.payByCard(card.getAccount().getId(), amount);
    }

    @Transactional
    @Override
    public void deleteCard(Long card_id) {
        cardRepository.deleteById(card_id);
    }

    private String createRandomCardNumber() {
        String cardNumber;
        Boolean exists = false;
        Random random = new Random();
        do {
            String firstPartNumber = String.format("%04d", random.nextInt(10000) % 10000);
            String secondPartNumber = String.format("%04d", random.nextInt(10000) % 10000);
            String thirdPartNumber = String.format("%04d", random.nextInt(10000) % 10000);
            String fourthPartNumber = String.format("%04d", random.nextInt(10000) % 10000);
            cardNumber = firstPartNumber + " " + secondPartNumber + " " + thirdPartNumber + " " + fourthPartNumber;
            exists = cardRepository.existsByCardNumber(cardNumber);
        } while (exists);

        return cardNumber;
    }

    private void validateCard(Card card) {
        if (card.getAccount().getStatusAccount() != StatusAccount.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Account is not active");
        }
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Card is expired");
        }

    }

}
