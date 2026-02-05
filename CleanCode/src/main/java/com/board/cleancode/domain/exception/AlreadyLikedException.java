package com.board.cleancode.domain.exception;

public class AlreadyLikedException extends RuntimeException {

    public AlreadyLikedException() {
        super("이미 좋아요한 게시글입니다.");
    }
}
