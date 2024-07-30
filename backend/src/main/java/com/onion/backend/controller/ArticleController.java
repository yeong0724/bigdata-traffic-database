package com.onion.backend.controller;

import com.onion.backend.dto.article.EditArticleDto;
import com.onion.backend.dto.article.WriteArticleDto;
import com.onion.backend.dto.article.Article;
import com.onion.backend.dto.article.ArticleSearch;
import com.onion.backend.service.ArticleService;
import com.onion.backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@Tag(name = "ArticleController", description = "게시글 API")
public class ArticleController {
    private final ArticleService articleService;
    private final CommentService commentService;

    @Autowired
    public ArticleController(ArticleService articleService, CommentService commentService) {
        this.articleService = articleService;
        this.commentService = commentService;
    }

    @Operation(summary = "게시글 등록")
    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> writeArticle(
            @RequestBody WriteArticleDto writeArticleDto,
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId
    ) {
        Article article = articleService.writeArticle(writeArticleDto, boardId);

        return ResponseEntity.ok(article);
    }

    @GetMapping("/{boardId}/articles")
    @Operation(summary = "게시글 조회")
    public ResponseEntity<List<Article>> getArticle(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Long firstId
    ) {
        if (lastId != null) {
            return ResponseEntity.ok(articleService.getOldArticle(boardId, lastId));
        }

        if (firstId != null) {
            return ResponseEntity.ok(articleService.getNewArticle(boardId, firstId));
        }

        return ResponseEntity.ok(articleService.firstGetArticle(boardId));
    }

    @GetMapping("/{boardId}/articles/{articleId}")
    @Operation(summary = "게시글 조회(+ 댓글)")
    public ResponseEntity<ArticleSearch> getArticleWithComment(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @Parameter(description = "Article 테이블 Key 값", example = "1", required = true) @PathVariable Long articleId
    ) {
        ArticleSearch article = articleService.getArticleWithComment(boardId, articleId);
        return ResponseEntity.ok(article);
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> editArticle(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @Parameter(description = "Article 테이블 Key 값", example = "1", required = true) @PathVariable Long articleId,
            @RequestBody EditArticleDto editArticleDto
    ) {
        return ResponseEntity.ok(articleService.editArticle(boardId, articleId, editArticleDto));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<String> deleteArticle(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @Parameter(description = "Article 테이블 Key 값", example = "1", required = true) @PathVariable Long articleId
    ) {
        articleService.deleteArticle(boardId, articleId);
        return ResponseEntity.ok("article is deleted");
    }
}
