package com.nikunj.library.dto;

public class LoginResponse {

    private String accessToken;
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
    }

    public LoginResponse(String token) {
        this.token = token;
        this.accessToken = token;
        this.tokenType = "Bearer";
    }

    public String getAccessToken() {
        return accessToken != null ? accessToken : token;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.token = accessToken;
    }

    public String getToken() {
        return token != null ? token : accessToken;
    }

    public void setToken(String token) {
        this.token = token;
        this.accessToken = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}