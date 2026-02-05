package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.model.Comment;

import java.util.List;

public interface GetCommentListUseCase {

    CommentPageResult getComments(Long postId, int page, int size);

    long countActiveComments(Long postId);

    record CommentPageResult(
            List<Comment> content,
            int page,
            int size,
            long totalElements,
            boolean hasMore
    ) {
    }
}
