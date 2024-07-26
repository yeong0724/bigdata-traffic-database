package com.onion.backend.controller;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Article> writeArticle(@RequestBody WriteArticleDto writeArticleDto, @PathVariable Long boardId) {
        writeArticleDto.setBoardId(boardId);

        Article article = articleService.writeArticle(writeArticleDto);

        return ResponseEntity.ok(article);
    }
}
