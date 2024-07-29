package com.onion.backend.mapper;

import com.onion.backend.entity.comment.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    Comment findById(@Param("commentId") Long commentId);

    Comment findLatestCommentOrderByCreatedDate(@Param("username") String username);

    Comment findLatestCommentOrderByUpdatedDate(@Param("username") String username);

    int createComment(Comment comment);

    int updateComment(Comment comment);

    int deleteComment(Comment comment);
}
