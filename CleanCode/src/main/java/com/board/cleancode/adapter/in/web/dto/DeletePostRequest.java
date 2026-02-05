package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.DeletePostUseCase.DeletePostCommand;
import jakarta.validation.constraints.NotBlank;

public record DeletePostRequest(
        @NotBlank String password
) {
    public DeletePostCommand toCommand(Long id) {
        return new DeletePostCommand(id, password);
    }
}
