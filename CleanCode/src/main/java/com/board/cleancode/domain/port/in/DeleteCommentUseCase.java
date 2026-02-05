package com.board.cleancode.domain.port.in;

public interface DeleteCommentUseCase {

    void deleteComment(DeleteCommentCommand command);

    record DeleteCommentCommand(
            Long postId,
            Long commentId,
            String password
    ) {
    }
}
