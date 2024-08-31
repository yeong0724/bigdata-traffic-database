package com.onion.backend.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Article {
    private Long id;

    private String title;

    private String content;

    private Long userId;

    private Long boardId;

    private Boolean isDeleted;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    private Long viewCount = 0L;

    public void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    public void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}