package io.dexi.oauth.payloads;

public class OAuthRedirectRequest {
    private String state;

    private String returnUrl;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
