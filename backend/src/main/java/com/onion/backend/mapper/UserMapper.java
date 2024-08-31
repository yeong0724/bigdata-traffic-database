package com.onion.backend.mapper;

import com.onion.backend.dto.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    int createUser(User user);

    int deleteUser(@Param("id") Long id);
}
