package com.chatforyou.io.services.impl;

import com.chatforyou.io.exception.CustomMessagingException;
import com.chatforyou.io.services.MailService;
import com.chatforyou.io.utils.ThreadUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {
    private final Map<String, String> mailValidateInfo = new ConcurrentHashMap<>();
    private final JavaMailSender mailSender;

    @Override
    public String sendEmailValidate(String email) throws MessagingException, UnsupportedEncodingException {
        String ePw = createKey();
        String bodyText = createBodyText(ePw);
        MimeMessage message = createMessage(email, bodyText);

        // send 라는 job 을 최대 5번, 1000ms 마다 반복
        ThreadUtils.runTask(() -> {
            try {
                mailSender.send(message);
                return true;
            } catch (Exception e) {
                log.error("Exception occurred while sending mail", e);
                return false;
            }
        }, 5, 1000, "Send Mail Job");
        mailValidateInfo.put(email, ePw);

        return ePw;
    }

    private String createKey() {
        StringBuilder key = new StringBuilder();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤, rnd 값에 따라서 아래 switch 문이 실행됨

            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    // a~z (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    // A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }

        return key.toString();
    }

    private String createBodyText(String ePw){
        StringBuilder msg = new StringBuilder();
        msg.append("<div style='margin:100px; color:#000000 !important;'>")
                .append("<h1 style='color:#000000 !important;'>안녕하세요.</h1>")
                .append("<h1 style='color:#000000 !important;'>언제나 즐거움을 선사하는 <span style='color:#3a84f8;'>ChatForYou.io</span> 입니다.</h1>")
                .append("<br>")
                .append("<p style='color:#000000 !important;'>아래 코드를 회원가입 창으로 돌아가 입력해주세요.</p>")
                .append("<p style='color:#000000 !important;'>본 이메일은 발신 전용 이메일로, 회신이 불가능합니다.</p>")
                .append("<br>")
                .append("<br>")
                .append("<br>")
                .append("<div align='center' style='border:1px solid black; font-family:verdana;'>")
                .append("<h3 style='color:#3a84f8;'>회원가입 인증 코드</h3>")
                .append("<div style='font-size:130%; color:#000000 !important;'>")
                .append("CODE : ")
                .append("<strong style='color:#000000 !important;'>" + ePw + "</strong><br/>")
                .append("</div>")
                .append("</div>")
                .append("</div>");

        return msg.toString();
    }

    private MimeMessage createMessage(String to, String text) throws MessagingException, UnsupportedEncodingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.toString());

            messageHelper.setTo(to); // 보내는 대상
            messageHelper.setSubject("ChatForYou.io 회원가입 이메일 인증");// 제목
            messageHelper.setText(text, true); // 내용, charset 타입, subtype

            // 보내는 사람의 이메일 주소, 보내는 사람 이름
            messageHelper.setFrom("no-reply@chatforyou.io", "no-reply@chatforyou.io"); // 보내는 사람

            return message;
        } catch (Exception e) {
            throw new CustomMessagingException("Exception occurred while creating message", to, e);
        }
    }
}
