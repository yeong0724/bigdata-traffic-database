package com.onion.backend.service;

import com.onion.backend.dto.SignUpUser;
import com.onion.backend.entity.User;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(SignUpUser signUpUser) {
        User user = new User();
        user.setUsername(signUpUser.getUsername());
        user.setPassword(signUpUser.getPassword());
        user.setEmail(signUpUser.getEmail());
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
