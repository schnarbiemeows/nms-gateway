package com.schnarbiesnmeowers.nmsgateway.exceptions.user;

public class TokenExpiredException extends RuntimeException {

    private static final long serialVersionUID = -7076928975713577708L;

    public TokenExpiredException(String message) {
        super(message);
    }
}
