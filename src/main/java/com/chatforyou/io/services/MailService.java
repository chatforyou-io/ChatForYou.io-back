package com.chatforyou.io.services;

import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface MailService {
    String sendEmailValidate(String email) throws MessagingException, UnsupportedEncodingException;
}
