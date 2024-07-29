package com.onion.backend.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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
