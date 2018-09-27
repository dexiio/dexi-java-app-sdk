package io.dexi.oauth.payloads;

import lombok.Data;

@Data
public class OAuthRedirectRequest {
    private String state;

    private String returnUrl;
}
