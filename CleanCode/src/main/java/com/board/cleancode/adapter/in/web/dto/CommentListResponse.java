package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.model.Comment;
import com.board.cleancode.domain.port.in.GetCommentListUseCase.CommentPageResult;

import java.util.List;
import java.util.Map;

public record CommentListResponse(
        List<CommentResponse> content,
        int page,
        int size,
        long totalElements,
        boolean hasMore
) {
    public static CommentListResponse from(CommentPageResult result) {
        List<CommentResponse> comments = result.content().stream()
                .map(CommentResponse::from)
                .toList();
        return new CommentListResponse(
                comments,
                result.page(),
                result.size(),
                result.totalElements(),
                result.hasMore()
        );
    }

    public static CommentListResponse fromWithReplyCounts(CommentPageResult result, Map<Long, Long> replyCounts) {
        List<CommentResponse> comments = result.content().stream()
                .map(comment -> CommentResponse.fromWithReplyCount(
                        comment,
                        replyCounts.getOrDefault(comment.getId(), 0L)
                ))
                .toList();
        return new CommentListResponse(
                comments,
                result.page(),
                result.size(),
                result.totalElements(),
                result.hasMore()
        );
    }
}
