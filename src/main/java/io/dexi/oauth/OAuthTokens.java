package io.dexi.oauth;


import lombok.Data;

@Data
public class OAuthTokens {
    private String name;

    private String email;

    private String provider;

    private String accessToken;

    private String refreshToken;

    private boolean valid;


    public OAuthTokens() {
    }

    public OAuthTokens(EncryptedOAuthTokens tokens) {
        this.name = tokens.getName();
        this.email = tokens.getEmail();
        this.provider = tokens.getProvider();
        this.valid = tokens.isValid();
    }
}
