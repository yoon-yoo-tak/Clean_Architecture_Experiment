package com.board.cleancode.domain.port.out;

public interface PasswordEncryptorPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
