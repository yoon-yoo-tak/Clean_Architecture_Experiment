package com.board.cleancode.domain.exception;

public class NestedReplyNotAllowedException extends RuntimeException {

    public NestedReplyNotAllowedException() {
        super("답글에는 답글을 달 수 없습니다.");
    }
}
