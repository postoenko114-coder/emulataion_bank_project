package com.example.demo.repositories;

import com.example.demo.models.card.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Boolean existsByCardNumber(String cardNumber);

    Optional<Card> findByCardNumber(String cardNumber);
}
