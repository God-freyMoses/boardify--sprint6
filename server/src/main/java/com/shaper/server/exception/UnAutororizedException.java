package com.shaper.server.exception;

public class UnAutororizedException extends RuntimeException {
    public UnAutororizedException(String message) {
        super(message);
    }
}
