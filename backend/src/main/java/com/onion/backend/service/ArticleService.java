package com.onion.backend.service;

import com.onion.backend.dto.EditArticleDto;
import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.User;
import com.onion.backend.exception.RateLimitException;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Date;
import java.util.Optional;

@Service
public class ArticleService {
    private final BoardRepository boardRepository;

    private final ArticleRepository articleRepository;

    private final UserRepository userRepository;

    @Autowired
    public ArticleService(BoardRepository boardRepository, ArticleRepository articleRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    private String getLoginUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    };

    public Article writeArticle(WriteArticleDto writeArticleDto, Long boardId) {
        if (!this.isCanWriteArticle()) {
            throw new RateLimitException("article not written by rate limit");
        }

        Optional<User> author = userRepository.findByUsername(getLoginUsername());
        Optional<Board> board = boardRepository.findById(boardId);

        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }

        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }

        Article article = new Article();
        article.setBoard(board.get()); // Optional
        article.setAuthor(author.get()); // Optional
        article.setTitle(writeArticleDto.getTitle());
        article.setContent(writeArticleDto.getContent());

        articleRepository.save(article);

        return article;
    };

    public List<Article> firstGetArticle(Long boardId) {
        return articleRepository.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }

    public List<Article> getOldArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, articleId);
    }

    public List<Article> getNewArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, articleId);
    }

    public Article editArticle(Long boardId, Long articleId, EditArticleDto editArticleDto) {
        Optional<Board> board = boardRepository.findById(boardId);

        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }

        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("article not found");
        }

        if (!this.isCanEditArticle()) {
            throw new RateLimitException("article not edited by rate limit");
        }

        if (editArticleDto.getTitle() != null) {
            article.get().setTitle(editArticleDto.getTitle());
        }

        if (editArticleDto.getContent() != null) {
            article.get().setContent(editArticleDto.getContent());
        }

        articleRepository.save(article.get());

        return article.get();
    }

    private boolean isCanWriteArticle() {
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByCreatedDate(getLoginUsername());
        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getCreatedDate());
    }

    private boolean isCanEditArticle() {
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByUpdatedDate(getLoginUsername());
        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getUpdatedDate());
    }

    public boolean isDifferenceMoreThanFiveMinutes(LocalDateTime localDateTime) {
        LocalDateTime dateAsLocalDateTime = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(localDateTime, dateAsLocalDateTime);

        return Math.abs(duration.toMinutes()) > 5;
    }
}
