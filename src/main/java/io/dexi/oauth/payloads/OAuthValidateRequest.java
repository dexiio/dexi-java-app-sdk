package io.dexi.oauth.payloads;

import lombok.Data;

@Data
public class OAuthValidateRequest {
    private String code;

    private String redirectUrl;
}
