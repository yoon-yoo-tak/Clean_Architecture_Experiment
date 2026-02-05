package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.CreateCommentUseCase.CreateCommentCommand;
import com.board.cleancode.domain.port.in.CreateReplyUseCase.CreateReplyCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank @Size(min = 1, max = 50) String author,
        @NotBlank @Size(min = 4) String password,
        @NotBlank String content
) {
    public CreateCommentCommand toCommand(Long postId) {
        return new CreateCommentCommand(postId, author, password, content);
    }

    public CreateReplyCommand toReplyCommand(Long postId, Long parentCommentId) {
        return new CreateReplyCommand(postId, parentCommentId, author, password, content);
    }
}
