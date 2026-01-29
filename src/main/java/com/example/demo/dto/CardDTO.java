package com.example.demo.dto;

import com.example.demo.models.card.StatusCard;
import com.example.demo.models.card.TypeCard;

import java.time.LocalDate;

public class CardDTO {
    private Long id;

    private String cardNumber;

    private LocalDate expiryDate;

    private String cardHolderName;

    private TypeCard typeCard;

    private StatusCard statusCard;

    public CardDTO() {}

    public CardDTO(Long id, String cardNumber, LocalDate expiryDate, String cardHolderName,  TypeCard typeCard, StatusCard statusCard) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cardHolderName = cardHolderName;
        this.typeCard = typeCard;
        this.statusCard = statusCard;
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public String getCardHolderName() {return cardHolderName;}

    public void setCardHolderName(String cardHolderName) {this.cardHolderName = cardHolderName;}

    public String getCardNumber() {return cardNumber;}

    public void setCardNumber(String cardNumber) {this.cardNumber = cardNumber;}

    public LocalDate getExpiryDate() {return expiryDate;}

    public void setExpiryDate(LocalDate expiryDate) {this.expiryDate = expiryDate;}

    public TypeCard getTypeCard() {return typeCard;}

    public void setTypeCard(TypeCard typeCard) {this.typeCard = typeCard;}

    public StatusCard getStatusCard() {return statusCard;}

    public void setStatusCard(StatusCard statusCard) {this.statusCard = statusCard;}
}
