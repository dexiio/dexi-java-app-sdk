package io.dexi.client;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DexiAuth {
    public static final String HEADER_ACTIVATION    = "X-DexiIO-Activation";

    public static final String HEADER_COMPONENT     = "X-DexiIO-Component";

    public static final String HEADER_AUTH_TYPE     = "X-DexiIO-AuthType";

    public static final String HEADER_ACCESS        = "X-DexiIO-Access";

    public static final String HEADER_ACCOUNT       = "X-DexiIO-Account";

    public static final String USER_AGENT           = "Dexi-Java-AppSDK/1.0";

    private final String accountId;

    private final String access;

    public static DexiAuth from(String accountId, String secret) {
        return new DexiAuth(accountId, secret);
    }

    public DexiAuth(String accountId, String secret) {
        this.accountId = accountId;
        this.access = calculateAccess(accountId, secret);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccess() {
        return access;
    }

    private static String calculateAccess(String clientId, String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update((clientId + secret).getBytes());

            return convertByteToHex(md.digest());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not available", e);
        }
    }

    private static String convertByteToHex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

}
