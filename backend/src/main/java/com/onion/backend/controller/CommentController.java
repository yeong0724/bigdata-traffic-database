package com.onion.backend.controller;

import com.onion.backend.dto.EditArticleDto;
import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Comment;
import com.onion.backend.service.ArticleService;
import com.onion.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Comment> writeComment(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @Parameter(description = "Article 테이블 Key 값", example = "1", required = true) @PathVariable Long articleId,
            @RequestBody WriteCommentDto writeCommentDto
    ) {
        return ResponseEntity.ok(commentService.writeComment(boardId, articleId, writeCommentDto));
    }
}
