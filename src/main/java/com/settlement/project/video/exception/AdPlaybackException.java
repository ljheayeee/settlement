package com.settlement.project.video.exception;


public class AdPlaybackException extends RuntimeException {

    public AdPlaybackException(String message) {
        super(message);
    }

    public AdPlaybackException(String message, Throwable cause) {
        super(message, cause);
    }
}