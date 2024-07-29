package com.onion.backend.entity.article;

import com.onion.backend.entity.board.BoardSearch;
import com.onion.backend.entity.comment.CommentSearch;
import com.onion.backend.entity.user.UserSearch;
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
}