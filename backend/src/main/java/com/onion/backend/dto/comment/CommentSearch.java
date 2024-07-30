package com.onion.backend.dto.comment;

import com.onion.backend.dto.user.UserSearch;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CommentSearch {
    private Long id;

    private String content;

    private UserSearch user;

    private Long commentsId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    private List<ChildComment> comments = new ArrayList<>();
}
