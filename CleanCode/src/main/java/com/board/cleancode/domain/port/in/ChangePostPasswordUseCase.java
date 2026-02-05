package com.board.cleancode.domain.port.in;

public interface ChangePostPasswordUseCase {

    void changePassword(ChangePostPasswordCommand command);

    record ChangePostPasswordCommand(
            Long postId,
            String currentPassword,
            String newPassword
    ) {}
}
