package io.dexi.oauth;

public class EncryptedOAuthTokens {
    private String name;

    private String email;

    private String provider;

    private String payload;

    private boolean valid;

    private String scope;

    private Long expiresInSeconds;

    public EncryptedOAuthTokens() {
    }

    public EncryptedOAuthTokens(OAuth2Tokens tokens) {
        this.name = tokens.getName();
        this.email = tokens.getEmail();
        this.provider = tokens.getProvider();
        this.valid = tokens.isValid();
    }

    public EncryptedOAuthTokens(OAuth1Tokens tokens) {
        this.name = tokens.getName();
        this.email = tokens.getEmail();
        this.provider = tokens.getProvider();
        this.valid = tokens.isValid();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
