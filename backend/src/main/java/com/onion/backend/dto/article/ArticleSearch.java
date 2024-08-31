package com.onion.backend.dto.article;

import com.onion.backend.dto.board.BoardSearch;
import com.onion.backend.dto.comment.CommentSearch;
import com.onion.backend.dto.user.UserSearch;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ArticleSearch {
    private Long id;

    private String title;

    private String content;

    private Boolean isDeleted;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    private UserSearch user;

    private BoardSearch board;

    private List<CommentSearch> comments = new ArrayList<>();

    private Long viewCount;
}