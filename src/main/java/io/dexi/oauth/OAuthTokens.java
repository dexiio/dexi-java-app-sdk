package io.dexi.oauth;

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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
