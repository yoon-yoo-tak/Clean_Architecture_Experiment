package com.board.cleancode.domain.port.in;

import com.board.cleancode.domain.model.Post;

import java.util.List;

public interface CreatePostUseCase {

    Post createPost(CreatePostCommand command);

    record CreatePostCommand(
            String title,
            String content,
            String author,
            String password,
            List<String> hashtags
    ) {
    }
}
