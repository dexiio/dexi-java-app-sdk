package io.dexi.oauth;


import lombok.Data;

@Data
public class EncryptedOAuthTokens {
    private String name;

    private String email;

    private String provider;

    private String payload;

    private boolean valid;

    public EncryptedOAuthTokens() {
    }

    public EncryptedOAuthTokens(OAuthTokens tokens) {
        this.name = tokens.getName();
        this.email = tokens.getEmail();
        this.provider = tokens.getProvider();
        this.valid = tokens.isValid();
    }
}
