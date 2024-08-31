package com.onion.backend.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class User {
    private Long id;

    private String username;

    private String password;

    private String email;

    private LocalDateTime lastLogin;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    public void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    public void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
