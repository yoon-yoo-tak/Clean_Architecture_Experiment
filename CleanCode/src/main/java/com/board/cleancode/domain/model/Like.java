package com.board.cleancode.domain.model;

public class Like {

    private Long id;
    private Long postId;
    private String guestId;

    private Like() {
    }

    public static Like create(Long postId, String guestId) {
        Like like = new Like();
        like.postId = postId;
        like.guestId = guestId;
        return like;
    }

    public static Like reconstitute(Long id, Long postId, String guestId) {
        Like like = new Like();
        like.id = id;
        like.postId = postId;
        like.guestId = guestId;
        return like;
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public String getGuestId() {
        return guestId;
    }
}
