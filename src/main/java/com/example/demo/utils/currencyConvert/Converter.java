package com.example.demo.utils.currencyConvert;

import com.example.demo.models.account.CurrencyAccount;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

public class Converter{

    private final RestTemplate restTemplate = new RestTemplate();

    public  BigDecimal getConvertToCurrency(CurrencyAccount currencyAccountFrom, CurrencyAccount currencyAccountTo, BigDecimal amount) {
        String url = "https://api.frankfurter.app/latest?from=" + currencyAccountFrom.toString() + "&to=" + currencyAccountTo.toString() ;

        CurrencyResponse response = restTemplate.getForObject(url, CurrencyResponse.class);

        if (response.getRates().get(currencyAccountTo.toString()) == null ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error with getting rates");
        }
        BigDecimal result = amount.multiply(response.getRates().get(currencyAccountTo.toString()));

        return result;
    }
}
