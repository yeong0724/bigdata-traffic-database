package com.onion.backend.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WriteCommentDto {
    @Schema(description = "게시글 댓글 내용", example = "article comment's content ~ !")
    @NotNull(message = "comment's content is mandatory")
    @NotBlank(message = "comment's content cannot be blank")
    String content;

    @Schema(description = "대댓글 부모 댓글 ID", example = "1")
    Long commentsId = null;
}