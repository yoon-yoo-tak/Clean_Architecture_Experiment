package com.board.cleancode.adapter.out.persistence;

import com.board.cleancode.domain.model.Like;
import jakarta.persistence.*;

@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"postId", "guestId"})
})
public class LikeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private String guestId;

    protected LikeJpaEntity() {
    }

    public static LikeJpaEntity fromDomain(Like like) {
        LikeJpaEntity entity = new LikeJpaEntity();
        entity.id = like.getId();
        entity.postId = like.getPostId();
        entity.guestId = like.getGuestId();
        return entity;
    }

    public Like toDomain() {
        return Like.reconstitute(id, postId, guestId);
    }
}
