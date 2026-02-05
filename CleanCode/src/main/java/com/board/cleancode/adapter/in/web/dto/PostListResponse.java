package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.GetPostListUseCase.PostListResult;

import java.util.List;

public record PostListResponse(
        long totalPostCount,
        long totalCommentCount,
        List<PostSummaryResponse> posts,
        int page,
        int size,
        int totalPages,
        long totalElements
) {
    public static PostListResponse from(PostListResult result) {
        List<PostSummaryResponse> posts = result.posts().stream()
                .map(PostSummaryResponse::from)
                .toList();
        return new PostListResponse(
                result.totalPostCount(),
                result.totalCommentCount(),
                posts,
                result.page(),
                result.size(),
                result.totalPages(),
                result.totalElements()
        );
    }
}
