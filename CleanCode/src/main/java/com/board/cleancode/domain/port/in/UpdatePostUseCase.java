package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.model.Post;

import java.util.List;

public interface UpdatePostUseCase {

    Post updatePost(UpdatePostCommand command);

    record UpdatePostCommand(
            Long id,
            String title,
            String content,
            String password,
            List<String> hashtags
    ) {
    }
}
