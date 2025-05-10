package com.chatforyou.io.services;

import com.chatforyou.io.models.JwtPayload;
import org.apache.coyote.BadRequestException;

import java.util.Map;

public interface JwtService {
    String createAccessToken(JwtPayload jwtPayload);
    String createRefreshToken(JwtPayload jwtPayload);
    void saveLastLoginDate(JwtPayload jwtPayload);
    JwtPayload validateAccessToken(String jwtToken) throws BadRequestException;
    JwtPayload validateRefreshToken(Long userIdx, String jwtToken) throws BadRequestException;
    Map<String, String> reissueToken(Long userIdx, String refreshToken) throws BadRequestException;
    void validateUserIdx(Long requestedUserIdx, Long tokenUserIdx) throws BadRequestException;
}
