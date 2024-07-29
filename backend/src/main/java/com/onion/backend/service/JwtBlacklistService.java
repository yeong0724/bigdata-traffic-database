package com.onion.backend.service;

import com.onion.backend.entity.jwt.JwtBlacklist;
import com.onion.backend.jwt.JwtUtil;
import com.onion.backend.mapper.JwtBlacklistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class JwtBlacklistService {

    private final JwtBlacklistMapper jwtBlacklistMapper;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtBlacklistService(JwtBlacklistMapper jwtBlacklistMapper, JwtUtil jwtUtil) {
        this.jwtBlacklistMapper = jwtBlacklistMapper;
        this.jwtUtil = jwtUtil;
    }

    public void blacklistToken(String token, LocalDateTime expirationTime, String username) {
        JwtBlacklist jwtBlacklist = new JwtBlacklist();
        jwtBlacklist.setToken(token);
        jwtBlacklist.setExpirationTime(expirationTime);
        jwtBlacklist.setUsername(username);
        jwtBlacklistMapper.createJwtBlacklist(jwtBlacklist);
    }

    public boolean isTokenBlacklisted(String currentToken) {
        String username = jwtUtil.getUsernameFromToken(currentToken);
        JwtBlacklist blacklistedToken = jwtBlacklistMapper.findTopByUsernameOrderByExpirationTime(username);
        if (blacklistedToken == null) {
            return false;
        }

        Instant instant = jwtUtil.getExpirationDateFromToken(currentToken).toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return blacklistedToken.getExpirationTime().isAfter(localDateTime.minusMinutes(60));
    }
}