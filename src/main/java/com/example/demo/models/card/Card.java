package com.example.demo.models.card;

import com.example.demo.dto.CardDTO;
import com.example.demo.models.account.Account;
import com.example.demo.models.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardNumber;

    private LocalDate expiryDate;

    /*
    CVV is not stored according to PCI DSS requirements.
    Used only during transaction processing.
    private String cvvCode;
    */

    private String cardHolderName;

    @Enumerated(EnumType.STRING)
    private StatusCard statusCard;

    @Enumerated(EnumType.STRING)
    private TypeCard typeCard;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public Card() {}

    public Card(String cardNumber, LocalDate expiryDate, String cardHolderName) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
    }

    public CardDTO toDTO() {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(id);
        cardDTO.setCardNumber(cardNumber);
        cardDTO.setExpiryDate(expiryDate);
        cardDTO.setCardHolderName(cardHolderName);
        cardDTO.setStatusCard(statusCard);
        cardDTO.setTypeCard(typeCard);
        return cardDTO;
    }


    public Account getAccount() {return account;}

    public void setAccount(Account account) {this.account = account;}

    public String getCardHolderName() {return cardHolderName;}

    public void setCardHolderName(String cardHolderName) {this.cardHolderName = cardHolderName;}

    public String getCardNumber() {return cardNumber;}

    public void setCardNumber(String cardNumber) {this.cardNumber = cardNumber;}

    public LocalDate getExpiryDate() {return expiryDate;}

    public void setExpiryDate(LocalDate expiryDate) {this.expiryDate = expiryDate;}

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

    public StatusCard getStatusCard() {return statusCard;}

    public void setStatusCard(StatusCard statusCard) {this.statusCard = statusCard;}

    public TypeCard getTypeCard() {return typeCard;}

    public void setTypeCard(TypeCard typeCard) {this.typeCard = typeCard;}

    public User getUser() {return user;}

    public void setUser(User user) {this.user = user;}


}
