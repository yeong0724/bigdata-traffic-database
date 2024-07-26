package com.onion.backend.service;

import com.onion.backend.common.CommonUtil;
import com.onion.backend.dto.EditArticleDto;
import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.dto.WriteCommentDto;
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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    private final BoardRepository boardRepository;

    private final ArticleRepository articleRepository;

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    @Autowired
    public CommentService(BoardRepository boardRepository, ArticleRepository articleRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment writeComment(Long boardId, Long articleId, WriteCommentDto writeCommentDto) {
        if (!this.isCanWriteComment()) {
            throw new RateLimitException("comment not written by rate limit");
        }

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

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthor(user);
        comment.setContent(writeCommentDto.getContent());

        commentRepository.save(comment);

        return comment;
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
        if (latestComment == null) {
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