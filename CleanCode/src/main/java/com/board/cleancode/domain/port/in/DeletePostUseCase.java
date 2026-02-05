package com.board.cleancode.domain.port.in;

public interface DeletePostUseCase {

    void deletePost(DeletePostCommand command);

    record DeletePostCommand(Long id, String password) {
    }
}
