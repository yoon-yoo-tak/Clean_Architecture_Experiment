package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.model.Post;
import com.board.cleancode.domain.port.in.GetPostListUseCase.PostSummary;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String author,
        LocalDateTime createdAt,
        int commentCount,
        int viewCount,
        int likeCount,
        @JsonProperty("isNew") boolean isNew
) {
    public static PostSummaryResponse from(PostSummary summary) {
        Post post = summary.post();
        boolean isNew = post.getCreatedAt().isAfter(LocalDateTime.now().minusDays(3));
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getAuthor(),
                post.getCreatedAt(),
                summary.commentCount(),
                post.getViewCount(),
                summary.likeCount(),
                isNew
        );
    }
}
