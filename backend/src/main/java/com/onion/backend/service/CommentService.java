package com.onion.backend.service;

import com.onion.backend.common.aop.annotation.CheckCommentEditable;
import com.onion.backend.common.aop.annotation.CheckCommentWriteable;
import com.onion.backend.common.utils.CommentContext;
import com.onion.backend.dto.comment.WriteCommentDto;
import com.onion.backend.dto.article.Article;
import com.onion.backend.dto.comment.Comment;
import com.onion.backend.dto.user.User;
import com.onion.backend.common.exception.DatabaseException;
import com.onion.backend.mapper.CommentMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentMapper commentMapper;

    @Autowired
    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Transactional
    @CheckCommentWriteable
    @SuppressWarnings("unused")
    public Comment writeComment(Long boardId, Long articleId, WriteCommentDto writeCommentDto) {
        User user = CommentContext.getCurrentUser();
        Article article = CommentContext.getCurrentArticle();

        Comment comment = new Comment();
        comment.setArticleId(article.getId()); // 게시글 정보
        comment.setUserId(user.getId()); // 작성자 정보
        comment.setContent(writeCommentDto.getContent());
        comment.setIsDeleted(false);
        comment.onCreate();

        Long commentsId = writeCommentDto.getCommentsId();
        if (commentsId != null) {
            comment.setCommentsId(commentsId);
        }

        int count = commentMapper.createComment(comment);

        if (count == 1) {
            return comment;
        } else {
            throw new DatabaseException("Comment create failed.");
        }
    }

    @Transactional
    @CheckCommentEditable
    @SuppressWarnings("unused")
    public Comment editComment(Long boardId, Long articleId, Long commentId, WriteCommentDto writeCommentDto) {
        Comment comment = CommentContext.getCurrentComment();
        comment.onUpdate();
        if (writeCommentDto.getContent() != null) {
            comment.setContent(writeCommentDto.getContent());
        }

        int count = commentMapper.updateComment(comment);
        if (count == 1) {
            return comment;
        } else {
            throw new DatabaseException("Comment update failed.");
        }
    }

    @Transactional
    @CheckCommentEditable
    @SuppressWarnings("unused")
    public void deleteComment(Long boardId, Long articleId, Long commentId) {
        Comment comment = CommentContext.getCurrentComment();
        comment.setIsDeleted(true);
        int count = commentMapper.deleteComment(comment);
        if (count < 1) {
            throw new DatabaseException("Comment deleted failed.");
        }
    }
}