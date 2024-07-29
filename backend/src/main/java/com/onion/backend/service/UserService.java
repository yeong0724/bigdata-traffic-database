package com.onion.backend.service;

import com.onion.backend.dto.SignUpUser;
import com.onion.backend.entity.user.User;
import com.onion.backend.exception.DatabaseException;
import com.onion.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(SignUpUser signUpUser) {
        User user = new User();
        user.setUsername(signUpUser.getUsername());
        user.setPassword(passwordEncoder.encode(signUpUser.getPassword()));
        user.setEmail(signUpUser.getEmail());
        user.onCreate();

        int count = userMapper.createUser(user);

        if (count == 1) {
            return user;
        } else {
            throw new DatabaseException("User Sign-up failed");
        }
    }

    public void deleteUser(Long userId) {
        int count = userMapper.deleteUser(userId);
        if (count != 1) {
            throw new DatabaseException("User delete failed");
        }
    }
}
