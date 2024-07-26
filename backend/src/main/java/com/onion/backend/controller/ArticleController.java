package com.onion.backend.controller;

import com.onion.backend.dto.EditArticleDto;
import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.service.ArticleService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class ArticleController {
    private final AuthenticationManager authenticationManager;

    private final ArticleService articleService;

    public ArticleController(AuthenticationManager authenticationManager, ArticleService articleService) {
        this.authenticationManager = authenticationManager;
        this.articleService = articleService;
    }

    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> writeArticle(
            @RequestBody WriteArticleDto writeArticleDto,
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId
    ) {
        Article article = articleService.writeArticle(writeArticleDto, boardId);

        return ResponseEntity.ok(article);
    }

    @GetMapping("/{boardId}/articles")
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

    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> editArticle(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @Parameter(description = "Article 테이블 Key 값", example = "1", required = true) @PathVariable Long articleId,
            @RequestBody EditArticleDto editArticleDto
    ) {
        return ResponseEntity.ok(articleService.editArticle(boardId, articleId, editArticleDto));
    }

    @DeleteMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<String> deleteArticle(
            @Parameter(description = "Board 테이블 Key 값", example = "1", required = true) @PathVariable Long boardId,
            @Parameter(description = "Article 테이블 Key 값", example = "1", required = true) @PathVariable Long articleId
    ) {
        articleService.deleteArticle(boardId, articleId);
        return ResponseEntity.ok("article is deleted");
    }
}
