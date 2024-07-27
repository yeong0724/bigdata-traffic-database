package com.onion.backend.aop;

import com.onion.backend.common.CommentContext;
import com.onion.backend.common.CommonUtil;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.Comment;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ForbiddenException;
import com.onion.backend.exception.RateLimitException;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.CommentRepository;
import com.onion.backend.repository.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Aspect
@Component
public class CommentAspect {
    private final UserRepository userRepository;

    private final BoardRepository boardRepository;

    private final ArticleRepository articleRepository;

    private final CommentRepository commentRepository;

    public CommentAspect(
            UserRepository userRepository,
            BoardRepository boardRepository,
            ArticleRepository articleRepository,
            CommentRepository commentRepository
    ) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    @Around("@annotation(com.onion.backend.aop.annotation.CheckCommentWriteable) && args(boardId, articleId, ..)")
    public Object checkWriteComment(ProceedingJoinPoint joinPoint, Long boardId, Long articleId) throws Throwable {
        if (!isCanWriteComment()) {
            throw new RateLimitException("comment not written by rate limit");
        }

        checkCommonConditions(boardId, articleId);

        try {
            return joinPoint.proceed();
        } finally {
            CommentContext.clear();
        }
    }

    @Around("@annotation(com.onion.backend.aop.annotation.CheckCommentEditable) && args(boardId, articleId, ..)")
    public Object checkEditComment(ProceedingJoinPoint joinPoint, Long boardId, Long articleId) throws Throwable {
        if (!isCanEditComment()) {
            throw new RateLimitException("comment not edited by rate limit");
        }

        checkCommonConditions(boardId, articleId);

        try {
            return joinPoint.proceed();
        } finally {
            CommentContext.clear();
        }
    }

    private void checkCommonConditions(Long boardId, Long articleId) {
        User user = userRepository.findByUsername(CommonUtil.getLoginUsername()).orElse(null);
        Board board = boardRepository.findById(boardId).orElse(null);
        Article article = articleRepository.findById(articleId).orElse(null);

        if (user == null) {
            throw new ResourceNotFoundException("author not found");
        }

        if (board == null) {
            throw new ResourceNotFoundException("board not found");
        }

        if (article == null) {
            throw new ResourceNotFoundException("article not found");
        }

        if (article.getIsDeleted()) {
            throw new ForbiddenException("article is deleted");
        }

        CommentContext.setCurrentUser(user);
        CommentContext.setCurrentArticle(article);
    }

    private boolean isCanWriteComment() {
        Comment latestComment = commentRepository.findLatestCommentOrderByCreatedDate(CommonUtil.getLoginUsername());
        if (latestComment == null) {
            return true;
        }

        return this.isDifferenceMoreThanOneMinutes(latestComment.getCreatedDate());
    }

    private boolean isCanEditComment() {
        Comment latestComment = commentRepository.findLatestCommentOrderByCreatedDate(CommonUtil.getLoginUsername());
        if (latestComment == null || latestComment.getUpdatedDate() == null) {
            return true;
        }

        return this.isDifferenceMoreThanOneMinutes(latestComment.getUpdatedDate());
    }

    private boolean isDifferenceMoreThanOneMinutes(LocalDateTime localDateTime) {
        LocalDateTime dateAsLocalDateTime = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(localDateTime, dateAsLocalDateTime);

        return Math.abs(duration.toMinutes()) > 1;
    }
}
