package com.example.demo.services.card;

import com.example.demo.dto.CardDTO;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface CardService {

    @Transactional
    CardDTO createCard(Long account_id, String typeCard);

    @Transactional
    List<CardDTO> getListUserCards(Long user_id);

    @Transactional
    CardDTO getCardById(Long card_id);

    @Transactional
    CardDTO findCardByNumber(String cardNumber);

    @Transactional
    void blockCard(Long card_id);

    @Transactional
    void closeCard(Long card_id);

    @Transactional
    void activateCard(Long card_id);

    @Transactional
    void changeTypeCard(Long card_id);

    @Transactional
    void payByCard(Long card_id, BigDecimal amount);

    @Transactional
    void deleteCard(Long card_id);
}
