package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.model.Post;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostResponse(
        Long id,
        String title,
        String content,
        String author,
        List<String> hashtags,
        int viewCount,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CommentListResponse comments,
        Boolean liked
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                post.getHashtags(),
                post.getViewCount(),
                0,
                0,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                null,
                null
        );
    }

    public static PostResponse from(Post post, CommentListResponse comments, int commentCount,
                                    int likeCount, boolean liked) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor(),
                post.getHashtags(),
                post.getViewCount(),
                likeCount,
                commentCount,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                comments,
                liked
        );
    }
}
