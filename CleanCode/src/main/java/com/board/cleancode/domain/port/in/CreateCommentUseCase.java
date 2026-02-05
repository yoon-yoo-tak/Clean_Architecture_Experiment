package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.model.Comment;

public interface CreateCommentUseCase {

    Comment createComment(CreateCommentCommand command);

    record CreateCommentCommand(
            Long postId,
            String author,
            String password,
            String content
    ) {
    }
}
