package com.chatforyou.io.services;

import com.chatforyou.io.models.JwtPayload;
import org.apache.coyote.BadRequestException;

import java.util.Map;

public interface JwtService {
    String createAccessToken(JwtPayload jwtPayload);
    String createRefreshToken(JwtPayload jwtPayload);
    JwtPayload verifyAccessToken(String jwtToken) throws BadRequestException;
    JwtPayload verifyRefreshToken(Long userIdx, String jwtToken) throws BadRequestException;
    Map<String, String> reissueToken(Long userIdx, String refreshToken) throws BadRequestException;
    void validateUserIdx(Long requestedUserIdx, Long tokenUserIdx) throws BadRequestException;
}
