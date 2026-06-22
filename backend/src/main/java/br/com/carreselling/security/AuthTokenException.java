package br.com.carreselling.security;

public class AuthTokenException extends RuntimeException {
    public AuthTokenException(String message) {
        super(message);
    }

    public AuthTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
