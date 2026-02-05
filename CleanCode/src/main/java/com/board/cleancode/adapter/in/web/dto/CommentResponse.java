package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.model.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long parentId,
        String author,
        String content,
        LocalDateTime createdAt,
        boolean deleted,
        Long replyCount
) {
    private static final String DELETED_CONTENT = "삭제된 댓글입니다.";

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getParentId(),
                comment.getAuthor(),
                comment.isDeleted() ? DELETED_CONTENT : comment.getContent(),
                comment.getCreatedAt(),
                comment.isDeleted(),
                null
        );
    }

    public static CommentResponse fromWithReplyCount(Comment comment, long replyCount) {
        return new CommentResponse(
                comment.getId(),
                comment.getParentId(),
                comment.getAuthor(),
                comment.isDeleted() ? DELETED_CONTENT : comment.getContent(),
                comment.getCreatedAt(),
                comment.isDeleted(),
                replyCount
        );
    }
}
