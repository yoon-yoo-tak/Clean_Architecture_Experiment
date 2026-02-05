package com.board.cleancode.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Post {

    private Long id;
    private String title;
    private String content;
    private String author;
    private String password;
    private List<String> hashtags;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Post() {
    }

    public static Post create(String title, String content, String author,
                              String encodedPassword, List<String> hashtags) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.author = author;
        post.password = encodedPassword;
        post.hashtags = hashtags != null ? new ArrayList<>(hashtags) : new ArrayList<>();
        post.viewCount = 0;
        post.createdAt = LocalDateTime.now();
        post.updatedAt = LocalDateTime.now();
        return post;
    }

    public static Post reconstitute(Long id, String title, String content, String author,
                                    String password, List<String> hashtags, int viewCount,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        Post post = new Post();
        post.id = id;
        post.title = title;
        post.content = content;
        post.author = author;
        post.password = password;
        post.hashtags = hashtags != null ? new ArrayList<>(hashtags) : new ArrayList<>();
        post.viewCount = viewCount;
        post.createdAt = createdAt;
        post.updatedAt = updatedAt;
        return post;
    }

    public void update(String title, String content, List<String> hashtags) {
        this.title = title;
        this.content = content;
        this.hashtags = hashtags != null ? new ArrayList<>(hashtags) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getHashtags() {
        return Collections.unmodifiableList(hashtags);
    }

    public int getViewCount() {
        return viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
