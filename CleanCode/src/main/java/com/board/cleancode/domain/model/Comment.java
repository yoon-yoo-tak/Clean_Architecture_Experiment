package com.board.cleancode.domain.model;

import java.time.LocalDateTime;

public class Comment {

    private Long id;
    private Long postId;
    private Long parentId;
    private String author;
    private String password;
    private String content;
    private boolean deleted;
    private LocalDateTime createdAt;

    private Comment() {
    }

    public static Comment create(Long postId, String author, String encodedPassword, String content) {
        Comment comment = new Comment();
        comment.postId = postId;
        comment.parentId = null;
        comment.author = author;
        comment.password = encodedPassword;
        comment.content = content;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    public static Comment createReply(Long postId, Long parentId, String author, String encodedPassword, String content) {
        Comment comment = new Comment();
        comment.postId = postId;
        comment.parentId = parentId;
        comment.author = author;
        comment.password = encodedPassword;
        comment.content = content;
        comment.deleted = false;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }

    public static Comment reconstitute(Long id, Long postId, Long parentId, String author, String password,
                                       String content, boolean deleted, LocalDateTime createdAt) {
        Comment comment = new Comment();
        comment.id = id;
        comment.postId = postId;
        comment.parentId = parentId;
        comment.author = author;
        comment.password = password;
        comment.content = content;
        comment.deleted = deleted;
        comment.createdAt = createdAt;
        return comment;
    }

    public boolean isReply() {
        return parentId != null;
    }

    public void markDeleted() {
        this.deleted = true;
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getAuthor() {
        return author;
    }

    public String getPassword() {
        return password;
    }

    public String getContent() {
        return content;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
