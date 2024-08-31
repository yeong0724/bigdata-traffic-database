package com.onion.backend.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Article Schema")
public class WriteArticleDto {
    @Schema(description = "게시글 제목", example = "My First Article")
    @NotNull(message = "Title is mandatory")
    @NotBlank(message = "Title cannot be blank")
    String title;

    @Schema(description = "게시글 내용", example = "My First Content")
    @NotNull(message = "Content is mandatory")
    @NotBlank(message = "Content cannot be blank")
    String content;
}