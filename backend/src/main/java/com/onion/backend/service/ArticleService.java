package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.common.utils.CommonUtil;
import com.onion.backend.dto.article.EditArticleDto;
import com.onion.backend.dto.article.WriteArticleDto;
import com.onion.backend.dto.article.Article;
import com.onion.backend.dto.article.ArticleSearch;
import com.onion.backend.dto.board.Board;
import com.onion.backend.dto.user.User;
import com.onion.backend.common.exception.DatabaseException;
import com.onion.backend.common.exception.ForbiddenException;
import com.onion.backend.common.exception.RateLimitException;
import com.onion.backend.common.exception.ResourceNotFoundException;
import com.onion.backend.mapper.ArticleMapper;
import com.onion.backend.mapper.BoardMapper;
import com.onion.backend.mapper.UserMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ArticleService {
    private final BoardMapper boardMapper;

    private final UserMapper userMapper;

    private final ArticleMapper articleMapper;

    private final ElasticSearchService elasticSearchService;

    private final ObjectMapper objectMapper;

    @Autowired
    public ArticleService(
            BoardMapper boardMapper,
            ArticleMapper articleMapper,
            UserMapper userMapper,
            ElasticSearchService elasticSearchService,
            ObjectMapper objectMapper
    ) {
        this.boardMapper = boardMapper;
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.elasticSearchService = elasticSearchService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ArticleSearch getArticleWithComment(Long boardId, Long articleId) throws JsonProcessingException {
        ArticleSearch articleSearch = articleMapper.findArticleByIdWithComments(boardId, articleId);

        if (articleSearch != null) {
            Long userId = articleSearch.getUser().getId();
            Long viewCount = articleSearch.getViewCount() + 1;
            Article article = Article.builder()
                    .id(articleId)
                    .title(articleSearch.getTitle())
                    .content(articleSearch.getContent())
                    .userId(userId)
                    .boardId(boardId)
                    .isDeleted(false)
                    .createdDate(articleSearch.getCreatedDate())
                    .viewCount(viewCount)
                    .build();

            article.onUpdate();

            // ElasticSearch 게시글 저장
            this.indexArticle(article);

            int count = articleMapper.updateArticle(article);
            if (count > 0) {
                articleSearch.setViewCount(viewCount);
                return articleSearch;
            } else {
                throw new DatabaseException("The view count update failed.");
            }
        } else {
            throw new DatabaseException("The post does not exist.");
        }
    }


    @Transactional
    public Article writeArticle(WriteArticleDto writeArticleDto, Long boardId) throws JsonProcessingException {
        if (!this.isCanWriteArticle()) {
            throw new RateLimitException("article not written by rate limit");
        }

        User user = userMapper.findByUsername(CommonUtil.getLoginUsername());
        Board board = boardMapper.findById(boardId);

        if (user == null) {
            throw new ResourceNotFoundException("author not found");
        }

        if (board == null) {
            throw new ResourceNotFoundException("board not found");
        }

        Article article = new Article();
        article.setBoardId(boardId);
        article.setUserId(user.getId());
        article.setTitle(writeArticleDto.getTitle());
        article.setContent(writeArticleDto.getContent());
        article.setIsDeleted(false);
        article.onCreate();

        int count = articleMapper.createArticle(article);

        // ElasticSearch 게시글 저장
        this.indexArticle(article);

        if (count == 1) {
            return article;
        } else {
            throw new DatabaseException("Comment create failed");
        }
    }

    public List<Article> firstGetArticle(Long boardId) {
        return articleMapper.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }

    public List<Article> getOldArticle(Long boardId, Long articleId) {
        return articleMapper.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, articleId);
    }

    public List<Article> getNewArticle(Long boardId, Long articleId) {
        return articleMapper.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, articleId);
    }

    @Transactional
    public Article editArticle(Long boardId, Long articleId, EditArticleDto editArticleDto) throws JsonProcessingException {
        Board board = boardMapper.findById(boardId);

        if (board == null) {
            throw new ResourceNotFoundException("board not found");
        }

        Article article = articleMapper.findById(articleId);

        if (article == null) {
            throw new ResourceNotFoundException("article not found");
        }

        User user = userMapper.findById(article.getUserId());
        String authorName = user.getUsername();
        String username = CommonUtil.getLoginUsername();

        if (!authorName.equals(username)) {
            throw new ForbiddenException("article author different");
        }

        if (!this.isCanEditArticle()) {
            throw new RateLimitException("article not edited by rate limit");
        }

        if (editArticleDto.getTitle() != null) {
            article.setTitle(editArticleDto.getTitle());
        }

        if (editArticleDto.getContent() != null) {
            article.setContent(editArticleDto.getContent());
        }

        article.onUpdate();
        int count = articleMapper.updateArticle(article);

        // ElasticSearch 게시글 저장
        this.indexArticle(article);

        if (count == 1) {
            return article;
        } else {
            throw new DatabaseException("Comment create failed");
        }
    }

    @Transactional
    public boolean deleteArticle(Long boardId, Long articleId) throws JsonProcessingException {
        Board board = boardMapper.findById(boardId);
        if (board == null) {
            throw new ResourceNotFoundException("board not found");
        }

        Article article = articleMapper.findById(articleId);

        if (article == null) {
            throw new ResourceNotFoundException("article not found");
        }

        User author = userMapper.findById(article.getUserId());
        String authorName = author.getUsername();
        String username = CommonUtil.getLoginUsername();

        if (!authorName.equals(username)) {
            throw new ForbiddenException("article author different");
        }

        article.setIsDeleted(true);
        articleMapper.deleteArticle(article);

        // ElasticSearch 게시글 저장
        this.indexArticle(article);

        return true;
    }

    private boolean isCanWriteArticle() {
        Article latestArticle = articleMapper.findLatestArticleByAuthorUsernameOrderByCreatedDate(CommonUtil.getLoginUsername());

        if (latestArticle == null) {
            return true;
        }

        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getCreatedDate());
    }

    private boolean isCanEditArticle() {
        Article latestArticle = articleMapper.findLatestArticleByAuthorUsernameOrderByUpdatedDate(CommonUtil.getLoginUsername());

        if (latestArticle == null || latestArticle.getUpdatedDate() == null) {
            return true;
        }

        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getUpdatedDate());
    }

    private boolean isDifferenceMoreThanFiveMinutes(LocalDateTime localDateTime) {
        LocalDateTime dateAsLocalDateTime = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(localDateTime, dateAsLocalDateTime);

        return Math.abs(duration.toMinutes()) > 5;
    }

    public String indexArticle(Article article) throws JsonProcessingException {
        String articleJson = objectMapper.writeValueAsString(article);
        return elasticSearchService.indexArticleDocument(article.getId().toString(), articleJson).block();
    }

    public List<Article> searchArticle(String keyword) {
        Mono<List<Long>> articleIds = elasticSearchService.articleSearch(keyword);

        try {
            List<Long> ids = articleIds.toFuture().get();
            if (!ids.isEmpty()) {
                return articleMapper.findAllByIds(ids);
            } else {
                return Collections.emptyList(); // 빈 리스트 반환
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
