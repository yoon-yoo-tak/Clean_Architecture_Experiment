package com.board.cleancode.domain.exception;

public class NotLikedException extends RuntimeException {

    public NotLikedException() {
        super("좋아요하지 않은 게시글입니다.");
    }
}
