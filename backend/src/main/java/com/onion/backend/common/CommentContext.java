package com.onion.backend.common;

import com.onion.backend.entity.article.Article;
import com.onion.backend.entity.comment.Comment;
import com.onion.backend.entity.user.User;
import org.springframework.stereotype.Component;

@Component
public class CommentContext {
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<Article> currentArticle = new ThreadLocal<>();
    private static final ThreadLocal<Comment> currentComment = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void clearCurrentUser() {
        currentUser.remove();
    }

    public static void setCurrentArticle(Article article) {
        currentArticle.set(article);
    }

    public static Article getCurrentArticle() {
        return currentArticle.get();
    }

    public static void clearCurrentArticle() {
        currentArticle.remove();
    }

    public static void setCurrentComment(Comment comment) {
        currentComment.set(comment);
    }

    public static Comment getCurrentComment() {
        return currentComment.get();
    }

    public static void clearCurrentComment() {
        currentComment.remove();
    }

    public static void clear() {
        clearCurrentUser();
        clearCurrentArticle();
        clearCurrentComment();
    }
}
