package com.onion.backend.aop;

import com.onion.backend.common.CommentContext;
import com.onion.backend.common.CommonUtil;
import com.onion.backend.entity.article.Article;
import com.onion.backend.entity.board.Board;
import com.onion.backend.entity.comment.Comment;
import com.onion.backend.entity.user.User;
import com.onion.backend.exception.ForbiddenException;
import com.onion.backend.exception.RateLimitException;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.mapper.ArticleMapper;
import com.onion.backend.mapper.BoardMapper;
import com.onion.backend.mapper.CommentMapper;
import com.onion.backend.mapper.UserMapper;
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
    private final UserMapper userMapper;

    private final ArticleMapper articleMapper;

    private final CommentMapper commentMapper;

    private final BoardMapper boardMapper;

    public CommentAspect(
            UserMapper userMapper,
            BoardMapper boardMapper,
            ArticleMapper articleMapper,
            CommentMapper commentMapper
    ) {
        this.userMapper = userMapper;
        this.boardMapper = boardMapper;
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
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

    @Around("@annotation(com.onion.backend.aop.annotation.CheckCommentEditable) && args(boardId, articleId, commentId, ..)")
    public Object checkEditComment(ProceedingJoinPoint joinPoint, Long boardId, Long articleId, Long commentId) throws Throwable {
        if (!isCanEditComment()) {
            throw new RateLimitException("comment not edited by rate limit");
        }

        checkCommonConditions(boardId, articleId);
        checkComment(commentId);

        try {
            return joinPoint.proceed();
        } finally {
            CommentContext.clear();
        }
    }

    private void checkCommonConditions(Long boardId, Long articleId) {
        User user = userMapper.findByUsername(CommonUtil.getLoginUsername());
        Board board = boardMapper.findById(boardId);
        Article article = articleMapper.findById(articleId);

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

    private void checkComment(Long commentId) {
        User user = CommentContext.getCurrentUser();
        Comment comment = commentMapper.findById(commentId);

        if (comment == null || comment.getIsDeleted()) {
            throw new ResourceNotFoundException("comment not found");
        }

        User author = userMapper.findById(comment.getUserId());;
        String authorName = author.getUsername();
        String username = user.getUsername();

        if (!authorName.equals(username)) {
            throw new ForbiddenException("comment author different");
        }

        CommentContext.setCurrentComment(comment);
    }

    private boolean isCanWriteComment() {
        Comment latestComment = commentMapper.findLatestCommentOrderByCreatedDate(CommonUtil.getLoginUsername());
        if (latestComment == null) {
            return true;
        }

        return this.isDifferenceMoreThanOneMinutes(latestComment.getCreatedDate());
    }

    private boolean isCanEditComment() {
        Comment latestComment = commentMapper.findLatestCommentOrderByUpdatedDate(CommonUtil.getLoginUsername());
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
