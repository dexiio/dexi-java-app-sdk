package io.dexi.oauth.payloads;

public class OAuth1RedirectResponse {

    private String requestToken;

    private String url;

    public String getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
