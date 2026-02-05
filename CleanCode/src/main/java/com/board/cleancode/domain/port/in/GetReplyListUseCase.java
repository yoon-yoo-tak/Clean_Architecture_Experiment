package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.model.Comment;

import java.util.List;

public interface GetReplyListUseCase {

    ReplyPageResult getReplies(Long postId, Long parentCommentId, int page, int size);

    long countActiveReplies(Long parentCommentId);

    record ReplyPageResult(
            List<Comment> content,
            int page,
            int size,
            long totalElements,
            boolean hasMore
    ) {
    }
}
