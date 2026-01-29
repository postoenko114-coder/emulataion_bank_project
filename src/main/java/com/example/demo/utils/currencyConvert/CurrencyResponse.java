package com.example.demo.utils.currencyConvert;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyResponse {
    private String base;
    private Map<String, BigDecimal> rates;

    public CurrencyResponse(String base, Map<String, BigDecimal> rates) {
        this.base = base;
        this.rates = rates;
    }

    public CurrencyResponse() {}

    public String getBase() {return base;}

    public void setBase(String base) {this.base = base;}

    public Map<String, BigDecimal> getRates() {return rates;}

    public void setRates(Map<String, BigDecimal> rates) {this.rates = rates;}
}