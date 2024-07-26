package com.onion.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class EditArticleDto {
    @Schema(description = "게시글 제목", example = "My First Article")
    String title;

    @Schema(description = "게시글 내용", example = "My First Content")
    String content;
}