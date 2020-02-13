package io.dexi.oauth;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class OAuthEncryptionService {

    private static final String ALGORITHM = "AES";

    private final String encryptionKey;

    private final Cipher encryptCipher;

    private final Cipher decryptCipher;

    public OAuthEncryptionService(String encryptionKey) {
        if (StringUtils.isBlank(encryptionKey)) {
            throw new IllegalArgumentException("Encryption key is required for the encryption service to work");
        }
        this.encryptionKey = encryptionKey;

        this.checkKey();

        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

        encryptCipher = getCipher(Cipher.ENCRYPT_MODE, secretKey);

        decryptCipher = getCipher(Cipher.DECRYPT_MODE, secretKey);
    }

    public void checkKey() {
        int keyLength = encryptionKey.getBytes().length;
        switch (keyLength) {
            case 16:
            case 24:
            case 32:
                return;
        }

        throw new RuntimeException("Invalid key length: " + keyLength + ". Must be 16, 24 or 32");
    }

    private String encrypt(String value) {
        return Base64.encodeBase64String(doFinal(encryptCipher, value.getBytes(StandardCharsets.UTF_8)));
    }
    public String decrypt(String cipherText) {
        final byte[] bytes = Base64.decodeBase64(cipherText);

        return new String(doFinal(decryptCipher, bytes), StandardCharsets.UTF_8);
    }

    public EncryptedOAuthTokens encrypt(OAuth2Tokens tokens) {
        EncryptedOAuthTokens out = new EncryptedOAuthTokens(tokens);

        String unencryptedPayload = String.format("%s:%s", tokens.getAccessToken(), tokens.getRefreshToken());

        out.setPayload(encrypt(unencryptedPayload));

        return out;
    }

    public EncryptedOAuthTokens encrypt(OAuth1Tokens tokens) {
        EncryptedOAuthTokens out = new EncryptedOAuthTokens(tokens);

        String unencryptedPayload = String.format("%s:%s", tokens.getAccessToken(), tokens.getAccessTokenSecret());

        out.setPayload(encrypt(unencryptedPayload));

        return out;
    }

    public OAuth2Tokens decrypt(EncryptedOAuthTokens tokens) {
        OAuth2Tokens out = new OAuth2Tokens(tokens);

        if (StringUtils.isNotBlank(tokens.getPayload())) {
            String unencryptedPayload = decrypt(tokens.getPayload());

            String[] parts = unencryptedPayload.split(":");

            if (parts.length == 2) {
                out.setAccessToken(parts[0]);
                out.setRefreshToken(parts[1]);
            }
        }

        return out;
    }

    public OAuth1Tokens decryptOAuth1(EncryptedOAuthTokens tokens) {
        OAuth1Tokens out = new OAuth1Tokens(tokens);

        if (StringUtils.isNotBlank(tokens.getPayload())) {
            String unencryptedPayload = decrypt(tokens.getPayload());

            String[] parts = unencryptedPayload.split(":");

            if (parts.length == 2) {
                out.setAccessToken(parts[0]);
                out.setAccessTokenSecret(parts[1]);
            }
        }

        return out;
    }

    private byte[] doFinal(Cipher cipher, byte[] value) {
        try {
            return cipher.doFinal(value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process cipher", e);
        }
    }

    private Cipher getCipher(int mode, SecretKeySpec secretKey) {
        try {
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, secretKey);
            return cipher;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get algorithm: " + ALGORITHM, e);
        }
    }
}
