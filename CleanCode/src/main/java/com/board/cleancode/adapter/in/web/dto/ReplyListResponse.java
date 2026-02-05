package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.GetReplyListUseCase.ReplyPageResult;

import java.util.List;

public record ReplyListResponse(
        List<CommentResponse> content,
        int page,
        int size,
        long totalElements,
        boolean hasMore
) {
    public static ReplyListResponse from(ReplyPageResult result) {
        List<CommentResponse> replies = result.content().stream()
                .map(CommentResponse::from)
                .toList();
        return new ReplyListResponse(
                replies,
                result.page(),
                result.size(),
                result.totalElements(),
                result.hasMore()
        );
    }
}
