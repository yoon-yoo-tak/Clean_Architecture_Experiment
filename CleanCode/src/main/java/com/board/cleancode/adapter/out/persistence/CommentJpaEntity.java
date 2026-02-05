package com.board.cleancode.adapter.out.persistence;

import com.board.cleancode.domain.model.Comment;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class CommentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column
    private Long parentId;

    @Column(nullable = false, length = 50)
    private String author;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected CommentJpaEntity() {
    }

    public static CommentJpaEntity fromDomain(Comment comment) {
        CommentJpaEntity entity = new CommentJpaEntity();
        entity.id = comment.getId();
        entity.postId = comment.getPostId();
        entity.parentId = comment.getParentId();
        entity.author = comment.getAuthor();
        entity.password = comment.getPassword();
        entity.content = comment.getContent();
        entity.deleted = comment.isDeleted();
        entity.createdAt = comment.getCreatedAt();
        return entity;
    }

    public Comment toDomain() {
        return Comment.reconstitute(
                id, postId, parentId, author, password,
                content, deleted, createdAt
        );
    }
}
