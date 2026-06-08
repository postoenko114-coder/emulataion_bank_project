package com.alex.bank.services.card;

import com.alex.bank.dto.CardDTO;
import com.alex.bank.mapper.CardMapper;
import com.alex.bank.models.account.Account;
import com.alex.bank.models.account.StatusAccount;
import com.alex.bank.models.card.Card;
import com.alex.bank.models.card.StatusCard;
import com.alex.bank.models.card.TypeCard;
import com.alex.bank.models.user.User;
import com.alex.bank.repositories.AccountRepository;
import com.alex.bank.repositories.CardRepository;
import com.alex.bank.repositories.UserRepository;
import com.alex.bank.services.account.AccountService;
import com.alex.bank.services.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final AccountService accountService;

    private final AccountRepository accountRepository;

    private final CardRepository cardRepository;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final CardMapper cardMapper;

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
        card.setTypeCard(parseCardType(typeCard));
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setUser(account.getUser());
        account.setCard(card);
        Card saved = cardRepository.save(card);
        log.info("Card created cardId={} userId={} accountId={} type={} cardNumber={}",
                saved.getId(), saved.getUser().getId(), account.getId(), saved.getTypeCard(), maskNumber(saved.getCardNumber()));
        return cardMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CardDTO> getListUserCards(Long user_id) {
        User user = userRepository.findById(user_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getCards().stream()
                .map(cardMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CardDTO getCardById(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        return cardMapper.toDTO(card);
    }

    @Transactional(readOnly = true)
    @Override
    public CardDTO findCardByNumber(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        return cardMapper.toDTO(card);
    }

    @Transactional
    @Override
    public CardDTO blockCard(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (card.getStatusCard() == StatusCard.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already blocked");
        } else {
            card.setStatusCard(StatusCard.BLOCKED);
            notificationService.notifyPersonalMessage(card.getUser().getId(), "Your card " + card.getCardNumber() + " has been blocked");
        }
        log.info("Card blocked cardId={} userId={}", card.getId(), card.getUser().getId());
        return cardMapper.toDTO(card);
    }

    @Transactional
    @Override
    public CardDTO closeCard(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if(card.getStatusCard() == StatusCard.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already closed");
        } else{
            card.setStatusCard(StatusCard.CLOSED);
            notificationService.notifyPersonalMessage(card.getUser().getId(), "Your card " + card.getCardNumber() + " has been closed");
        }
        log.info("Card closed cardId={} userId={}", card.getId(), card.getUser().getId());
        return cardMapper.toDTO(card);
    }

    @Transactional
    @Override
    public CardDTO activateCard(Long card_id){
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if(card.getStatusCard() == StatusCard.ACTIVE){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card already active");
        } else {
            card.setStatusCard(StatusCard.ACTIVE);
            notificationService.notifyPersonalMessage(card.getUser().getId(), "Your card " + card.getCardNumber() + " has been activated");
        }
        log.info("Card activated cardId={} userId={}", card.getId(), card.getUser().getId());
        return cardMapper.toDTO(card);
    }

    @Transactional
    @Override
    public CardDTO changeTypeCard(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (card.getTypeCard() == TypeCard.DEBIT) {
            card.setTypeCard(TypeCard.CREDIT);
        } else if (card.getTypeCard() == TypeCard.CREDIT) {
            card.setTypeCard(TypeCard.DEBIT);
        }
        log.info("Card type changed cardId={} userId={} type={}", card.getId(), getUserId(card), card.getTypeCard());
        return cardMapper.toDTO(card);
    }

    @Transactional
    @Override
    public void payByCard(Long card_id, BigDecimal amount) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        validateCard(card);

        accountService.payByCard(card.getAccount().getId(), amount);
        log.info("Card payment requested cardId={} accountId={} amount={}", card.getId(), card.getAccount().getId(), amount);
    }

    @Transactional
    @Override
    public void deleteCard(Long card_id) {
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        cardRepository.delete(card);
        log.info("Card deleted cardId={}", card_id);
    }

    private String maskNumber(String number) {
        if (number == null) {
            return "****";
        }
        String digits = number.replace(" ", "");
        if (digits.length() <= 4) {
            return "****";
        }
        return "**** **** **** " + digits.substring(digits.length() - 4);
    }

    private Long getUserId(Card card) {
        return card.getUser() != null ? card.getUser().getId() : null;
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

    private TypeCard parseCardType(String typeCard) {
        try {
            return TypeCard.valueOf(typeCard.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid card type");
        }
    }

}
