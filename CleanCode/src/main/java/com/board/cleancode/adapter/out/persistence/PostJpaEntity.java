package com.board.cleancode.adapter.out.persistence;

import com.board.cleancode.domain.model.Post;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class PostJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String author;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "hashtag", length = 30)
    private List<String> hashtags = new ArrayList<>();

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected PostJpaEntity() {
    }

    public static PostJpaEntity fromDomain(Post post) {
        PostJpaEntity entity = new PostJpaEntity();
        entity.id = post.getId();
        entity.title = post.getTitle();
        entity.content = post.getContent();
        entity.author = post.getAuthor();
        entity.password = post.getPassword();
        entity.hashtags = new ArrayList<>(post.getHashtags());
        entity.viewCount = post.getViewCount();
        entity.createdAt = post.getCreatedAt();
        entity.updatedAt = post.getUpdatedAt();
        return entity;
    }

    public Post toDomain() {
        return Post.reconstitute(
                id, title, content, author, password,
                new ArrayList<>(hashtags), viewCount,
                createdAt, updatedAt
        );
    }
}
