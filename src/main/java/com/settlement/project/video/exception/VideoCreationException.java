package com.settlement.project.video.exception;

public class VideoCreationException extends RuntimeException{
    public VideoCreationException(String message) {
        super(message);
    }

    public VideoCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
