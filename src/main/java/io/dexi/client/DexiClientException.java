package io.dexi.client;


public class DexiClientException extends Exception {
    public DexiClientException(String message) {
        super(message);
    }

    public DexiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
