package com.onion.backend.controller;

import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Comment;
import com.onion.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/boards")
@Tag(name = "CommentController", description = "댓글 API")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "댓글 등록")
    @PostMapping("/{boardId}/articles/{articleId}/comments")
    public ResponseEntity<Comment> writeComment(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @Parameter(description = "Article 테이블 Key 값", example = "1", required = true) @PathVariable Long articleId,
            @RequestBody WriteCommentDto writeCommentDto
    ) {
        return ResponseEntity.ok(commentService.writeComment(boardId, articleId, writeCommentDto));
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/{boardId}/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Comment> writeComment(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @Parameter(description = "Article 테이블 Key 값", example = "1", required = true) @PathVariable Long articleId,
            @Parameter(description = "Comment 테이블 Key 값", example = "1", required = true)@PathVariable Long commentId,
            @RequestBody WriteCommentDto editCommentDto
    ) {
        return ResponseEntity.ok(commentService.editComment(boardId, articleId, commentId, editCommentDto));
    }
}
