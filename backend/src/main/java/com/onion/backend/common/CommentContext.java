package com.onion.backend.common;

import com.onion.backend.entity.Article;
import com.onion.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentContext {
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<Article> currentArticle = new ThreadLocal<>();

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

    public static void clear() {
        clearCurrentUser();
        clearCurrentArticle();
    }
}
