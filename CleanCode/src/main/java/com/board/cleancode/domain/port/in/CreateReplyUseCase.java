package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.model.Comment;

public interface CreateReplyUseCase {

    Comment createReply(CreateReplyCommand command);

    record CreateReplyCommand(
            Long postId,
            Long parentCommentId,
            String author,
            String password,
            String content
    ) {
    }
}
