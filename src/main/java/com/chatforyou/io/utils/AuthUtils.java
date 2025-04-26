package com.chatforyou.io.utils;

import com.chatforyou.io.models.ValidateType;
import com.chatforyou.io.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    private final UserRepository userRepository;

    public String getEncodeStr(String str){
        return new String(Base64.getEncoder().encode(str.getBytes()));
    }

    public String getDecodeStr(byte[] str){
        return new String(Base64.getDecoder().decode(str));
    }

    public boolean validateStrByType(ValidateType type, String str) {
        switch (type) {
            case ID:
                return userRepository.checkExistsById(str);
            case NICKNAME:
                return userRepository.checkExistsByNickName(str);
            case PASSWORD:
                // TODO passwd validate 체크 필요
                break;
//			case CHATROOM_NAME:
//				return chatRoomRepository.checkExistsByRoomName(str);
        }

        return false;
    }
}
