package com.onion.backend.entity.comment;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Comment {
    private Long id;

    private String content;

    private Long userId;

    private Long articleId;

    private Boolean isDeleted;

    private Long commentsId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    public void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    public void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}