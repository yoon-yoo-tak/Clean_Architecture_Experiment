package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.DeleteCommentUseCase.DeleteCommentCommand;
import jakarta.validation.constraints.NotBlank;

public record DeleteCommentRequest(
        @NotBlank String password
) {
    public DeleteCommentCommand toCommand(Long postId, Long commentId) {
        return new DeleteCommentCommand(postId, commentId, password);
    }
}
