package com.chatforyou.io.exception;


import jakarta.mail.MessagingException;
import lombok.Getter;

@Getter
public class CustomMessagingException extends MessagingException {
    private final String recipient;  // 추가 정보: 수신자

    public CustomMessagingException(String message, String recipient, Exception cause) {
        super(message, cause);
        this.recipient = recipient;
    }
}
