package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.ChangePostPasswordUseCase.ChangePostPasswordCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 4) String newPassword
) {
    public ChangePostPasswordCommand toCommand(Long postId) {
        return new ChangePostPasswordCommand(postId, currentPassword, newPassword);
    }
}
