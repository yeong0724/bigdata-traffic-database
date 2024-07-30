package com.onion.backend.mapper;

import com.onion.backend.dto.article.Article;
import com.onion.backend.dto.article.ArticleSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {
    Article findById(@Param("articleId") Long articleId);

    int createArticle(Article article);

    int updateArticle(Article article);

    int deleteArticle(Article article);

    List<Article> findTop10ByBoardIdOrderByCreatedDateDesc(@Param("boardId") Long boardId);

    List<Article> findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(
            @Param("boardId") Long boardId,
            @Param("articleId") Long articleId
    );

    List<Article> findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(
            @Param("boardId") Long boardId,
            @Param("articleId") Long articleId
    );

    Article findLatestArticleByAuthorUsernameOrderByCreatedDate(@Param("username") String username);

    Article findLatestArticleByAuthorUsernameOrderByUpdatedDate(@Param("username") String username);

    ArticleSearch findArticleByIdWithComments(@Param("boardId") Long boardId, @Param("articleId") Long articleId);
}
