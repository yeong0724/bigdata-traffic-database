package com.onion.backend.service;

import com.onion.backend.aop.annotation.CheckCommentEditable;
import com.onion.backend.aop.annotation.CheckCommentWriteable;
import com.onion.backend.common.CommentContext;
import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.Comment;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ForbiddenException;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.CommentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class CommentService {
    private final BoardRepository boardRepository;

    private final ArticleRepository articleRepository;

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(BoardRepository boardRepository, ArticleRepository articleRepository, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    @CheckCommentWriteable
    @SuppressWarnings("unused")
    public Comment writeComment(Long boardId, Long articleId, WriteCommentDto writeCommentDto) {
        User user = CommentContext.getCurrentUser();
        Article article = CommentContext.getCurrentArticle();

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthor(user);
        comment.setContent(writeCommentDto.getContent());

        commentRepository.save(comment);

        return comment;
    }

    @Transactional
    @CheckCommentEditable
    @SuppressWarnings("unused")
    public Comment editComment(Long boardId, Long articleId, Long commentId, WriteCommentDto writeCommentDto) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null || comment.getIsDeleted()) {
            throw new ResourceNotFoundException("comment not found");
        }

        User user = CommentContext.getCurrentUser();
        if (!Objects.equals(comment.getAuthor().getUsername(), user.getUsername())) {
            throw new ForbiddenException("comment author different");
        }

        if (writeCommentDto.getContent() != null) {
            comment.setContent(writeCommentDto.getContent());
        }

        commentRepository.save(comment);
        return comment;
    }

    @Async
    protected CompletableFuture<Article> getArticle(Long boardId, Long articleId) {
        Optional<Board> board = boardRepository.findById(boardId);
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }
        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty() || article.get().getIsDeleted()) {
            throw new ResourceNotFoundException("article not found");
        }
        return CompletableFuture.completedFuture(article.get());
    }

    @Async
    protected CompletableFuture<List<Comment>> getComments(Long articleId) {
        return CompletableFuture.completedFuture(commentRepository.findByArticleId(articleId));
    }

    public CompletableFuture<Article> getArticleWithComment(Long boardId, Long articleId) {
        CompletableFuture<Article> articleFuture = this.getArticle(boardId, articleId);
        CompletableFuture<List<Comment>> commentsFuture = this.getComments(articleId);

        return CompletableFuture.allOf(articleFuture, commentsFuture)
                .thenApply(voidResult -> {
                    try {
                        Article article = articleFuture.get();
                        List<Comment> comments = commentsFuture.get();
                        article.setComments(comments);
                        return article;
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }
}