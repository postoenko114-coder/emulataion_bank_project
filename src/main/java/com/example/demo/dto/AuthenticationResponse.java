package com.example.demo.dto;

public class AuthenticationResponse {
    private String token;
    private String redirectUrl;

    public AuthenticationResponse() {}

    public AuthenticationResponse(String token, String redirectUrl) {
        this.token = token;
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {return redirectUrl;}

    public void setRedirectUrl(String redirectUrl) {this.redirectUrl = redirectUrl;}

    public String getToken() {return token;}

    public void setToken(String token) {this.token = token;}
}
