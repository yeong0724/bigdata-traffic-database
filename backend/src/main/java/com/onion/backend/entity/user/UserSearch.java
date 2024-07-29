package com.onion.backend.entity.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserSearch {
    private String username;

    private LocalDateTime lastLogin;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}
