package com.board.cleancode.domain.exception;

public class PasswordMismatchException extends RuntimeException {

    public PasswordMismatchException() {
        super("비밀번호가 일치하지 않습니다.");
    }
}
