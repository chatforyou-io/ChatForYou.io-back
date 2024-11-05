package com.chatforyou.io.services;

import com.chatforyou.io.controller.ExceptionController;
import com.chatforyou.io.models.JwtPayload;
import org.apache.coyote.BadRequestException;

import java.util.Map;

public interface JwtService {
    String createAccessToken(JwtPayload jwtPayload);
    String createRefreshToken(Long userIdx, JwtPayload jwtPayload);
    JwtPayload verifyAccessToken(String jwtToken) throws ExceptionController.JwtSignatureException, ExceptionController.JwtExpiredException, ExceptionController.JwtBearerException;
    JwtPayload verifyRefreshToken(Long userIdx, String jwtToken) throws BadRequestException;
    Map<String, String> reissueToken(Long userIdx, String refreshToken) throws ExceptionController.JwtBearerException, ExceptionController.JwtExpiredException, ExceptionController.JwtSignatureException;
}
