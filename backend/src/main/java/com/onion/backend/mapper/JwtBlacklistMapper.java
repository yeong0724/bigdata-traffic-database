package com.onion.backend.mapper;

import com.onion.backend.dto.jwt.JwtBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

@Mapper
public interface JwtBlacklistMapper {
    void createJwtBlacklist(JwtBlacklist jwtBlacklist);

    JwtBlacklist findTopByUsernameOrderByExpirationTime(@Param("username") String username);
}
