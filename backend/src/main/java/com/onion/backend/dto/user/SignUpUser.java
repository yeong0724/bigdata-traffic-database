package com.onion.backend.dto.user;

import lombok.Data;

@Data
public class SignUpUser {
    String username;
    String password;
    String email;
}
