package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.UpdatePostUseCase.UpdatePostCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdatePostRequest(
        @NotBlank @Size(min = 1, max = 200) String title,
        @NotBlank String content,
        @NotBlank String password,
        @Size(max = 5) List<@NotBlank @Size(min = 1, max = 30) String> hashtags
) {
    public UpdatePostCommand toCommand(Long id) {
        return new UpdatePostCommand(id, title, content, password, hashtags);
    }
}
