package com.board.cleancode.adapter.in.web.dto;

import com.board.cleancode.domain.port.in.CreatePostUseCase.CreatePostCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
        @NotBlank @Size(min = 1, max = 200) String title,
        @NotBlank String content,
        @NotBlank @Size(min = 1, max = 50) String author,
        @NotBlank @Size(min = 4) String password,
        @Size(max = 5) List<@NotBlank @Size(min = 1, max = 30) String> hashtags
) {
    public CreatePostCommand toCommand() {
        return new CreatePostCommand(title, content, author, password, hashtags);
    }
}
